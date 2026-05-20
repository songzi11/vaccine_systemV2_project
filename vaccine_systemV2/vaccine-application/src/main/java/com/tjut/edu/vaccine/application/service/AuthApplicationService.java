package com.tjut.edu.vaccine.application.service;

import com.tjut.edu.vaccine.application.assembler.AuthAssembler;
import com.tjut.edu.vaccine.application.dto.request.LoginRequest;
import com.tjut.edu.vaccine.application.dto.request.RegisterRequest;
import com.tjut.edu.vaccine.application.dto.request.SmsRequest;
import com.tjut.edu.vaccine.application.dto.response.AuthResponse;
import com.tjut.edu.vaccine.application.dto.response.UserInfoResponse;
import com.tjut.edu.vaccine.common.enums.ErrorCode;
import com.tjut.edu.vaccine.common.enums.Gender;
import com.tjut.edu.vaccine.common.enums.IdCardType;
import com.tjut.edu.vaccine.common.exception.BusinessException;
import com.tjut.edu.vaccine.domain.identity.aggregate.Role;
import com.tjut.edu.vaccine.domain.identity.aggregate.User;
import com.tjut.edu.vaccine.domain.identity.entity.UserRole;
import com.tjut.edu.vaccine.domain.identity.entity.VerifyCode;
import com.tjut.edu.vaccine.domain.identity.repository.RoleRepository;
import com.tjut.edu.vaccine.domain.identity.repository.UserRepository;
import com.tjut.edu.vaccine.domain.identity.repository.UserRoleRepository;
import com.tjut.edu.vaccine.domain.identity.repository.VerifyCodeRepository;
import com.tjut.edu.vaccine.domain.identity.repository.HospitalWindowRepository;
import com.tjut.edu.vaccine.domain.port.SecurityContextPort;
import com.tjut.edu.vaccine.domain.port.TokenServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private static final String SMS_KEY_PREFIX = "sms:register:";
    private static final long SMS_EXPIRE_MINUTES = 5;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final VerifyCodeRepository verifyCodeRepository;
    private final HospitalWindowRepository hospitalWindowRepository;
    private final TokenServicePort tokenServicePort;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final SecurityContextPort securityContextPort;

    public AuthResponse register(RegisterRequest req) {
        // 1. 医生注册需验证注册验证码
        VerifyCode verifyCode = null;
        if ("DOCTOR".equals(req.getRoleType())) {
            if (req.getVerifyCode() == null || req.getVerifyCode().isBlank()) {
                throw new BusinessException(ErrorCode.VERIFY_CODE_INVALID);
            }
            verifyCode = verifyCodeRepository.findByCode(req.getVerifyCode().trim());
            if (verifyCode == null) {
                throw new BusinessException(ErrorCode.VERIFY_CODE_INVALID);
            }
            if (verifyCode.getStatus() == VerifyCode.STATUS_USED) {
                throw new BusinessException(ErrorCode.VERIFY_CODE_USED);
            }
            if (verifyCode.getStatus() == VerifyCode.STATUS_REVOKED) {
                throw new BusinessException(ErrorCode.VERIFY_CODE_REVOKED);
            }
            if (!verifyCode.isUsable()) {
                throw new BusinessException(ErrorCode.VERIFY_CODE_INVALID);
            }
        }

        // 2. 检查手机号是否已注册
        if (userRepository.findByPhone(req.getPhone()) != null) {
            throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
        }

        // 3. 创建用户
        String encodedPassword = passwordEncoder.encode(req.getPassword());
        User user = User.register(req.getPhone(), encodedPassword, req.getRealName());
        userRepository.save(user);

        // 4. 分配默认角色
        Role defaultRole = roleRepository.findByRoleCode("USER");
        userRoleRepository.save(new UserRole(user.getId().value(), defaultRole.getId().value()));

        // 5. 医生注册标记验证码已使用
        if (verifyCode != null) {
            verifyCode.markUsed(user.getId().value());
            verifyCodeRepository.update(verifyCode);
        }

        // 6. 生成 JWT
        List<String> roleCodes = getUserRoleCodes(user.getId().value());
        String token = tokenServicePort.generateToken(user.getId().value(), user.getPhone(), roleCodes);

        return AuthAssembler.toAuthResponse(user, token, roleCodes);
    }

    public AuthResponse login(LoginRequest req) {
        // 1. 查找用户
        User user = userRepository.findByPhone(req.getPhone());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_LOGIN_FAILED);
        }

        // 2. 检查登录状态
        if (!user.isLoginAllowed()) {
            throw new BusinessException(ErrorCode.USER_FROZEN_LOGIN);
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_LOGIN_FAILED);
        }

        // 4. 生成 JWT
        List<String> roleCodes = getUserRoleCodes(user.getId().value());
        String token = tokenServicePort.generateToken(user.getId().value(), user.getPhone(), roleCodes);

        return AuthAssembler.toAuthResponse(user, token, roleCodes);
    }

    public String sendSmsCode(SmsRequest req) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        stringRedisTemplate.opsForValue().set(
            SMS_KEY_PREFIX + req.getPhone(), code, SMS_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.info("验证码已发送: phone={}, code={}", req.getPhone(), code);
        return code;
    }

    public UserInfoResponse getCurrentUser() {
        Long userId = securityContextPort.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        List<String> roleCodes = getUserRoleCodes(userId);
        UserInfoResponse response = AuthAssembler.toUserInfoResponse(user, roleCodes);

        // 医生用户：查询其分配的窗口信息
        if (roleCodes.stream().anyMatch(r -> r.startsWith("DOCTOR_"))) {
            hospitalWindowRepository.findByDoctorId(userId)
                    .ifPresent(window -> {
                        response.setWindowCode(window.getWindowCode());
                        response.setWindowName(window.getWindowName());
                        response.setWindowFunctionType(window.getWindowFunctionType());
                    });
        }

        return response;
    }

    public void changePassword(String oldPassword, String newPassword) {
        Long userId = securityContextPort.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public AuthResponse smsLogin(String phone, String smsCode) {
        // 1. 验证短信验证码 (use a different prefix for login)
        String cachedCode = stringRedisTemplate.opsForValue().get(SMS_KEY_PREFIX + phone);
        if (cachedCode == null || !cachedCode.equals(smsCode)) {
            throw new BusinessException(ErrorCode.SMS_CODE_INVALID);
        }
        stringRedisTemplate.delete(SMS_KEY_PREFIX + phone);

        // 2. 查找用户
        User user = userRepository.findByPhone(phone);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_LOGIN_FAILED);
        }
        if (!user.isLoginAllowed()) {
            throw new BusinessException(ErrorCode.USER_FROZEN_LOGIN);
        }

        // 3. 生成 JWT
        List<String> roleCodes = getUserRoleCodes(user.getId().value());
        String token = tokenServicePort.generateToken(user.getId().value(), user.getPhone(), roleCodes);

        return AuthAssembler.toAuthResponse(user, token, roleCodes);
    }

    public void logout() {
        Long userId = securityContextPort.getCurrentUserId();
        log.info("用户登出: userId={}", userId);
        // JWT是无状态的，客户端清除token即可
    }

    public void resetPassword(String phone, String smsCode, String newPassword) {
        // 1. 验证短信验证码
        String cachedCode = stringRedisTemplate.opsForValue().get(SMS_KEY_PREFIX + phone);
        if (cachedCode == null || !cachedCode.equals(smsCode)) {
            throw new BusinessException(ErrorCode.SMS_CODE_INVALID);
        }
        stringRedisTemplate.delete(SMS_KEY_PREFIX + phone);

        // 2. 查找用户
        User user = userRepository.findByPhone(phone);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("密码重置成功: phone={}", phone);
    }

    public UserInfoResponse updateProfile(Map<String, String> body) {
        Long userId = securityContextPort.getCurrentUserId();
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (body.containsKey("realName")) {
            user.setRealName(body.get("realName"));
        }
        if (body.containsKey("gender")) {
            // gender is stored as enum in User entity
            try {
                user.setGender(Gender.fromCode(Integer.parseInt(body.get("gender"))));
            } catch (IllegalArgumentException ignored) {}
        }
        if (body.containsKey("idCardType")) {
            try {
                user.setIdCardType(IdCardType.fromCode(Integer.parseInt(body.get("idCardType"))));
            } catch (IllegalArgumentException ignored) {}
        }
        if (body.containsKey("idCardNo")) {
            user.setIdCardNo(body.get("idCardNo"));
        }
        userRepository.save(user);
        List<String> roleCodes = getUserRoleCodes(userId);
        return AuthAssembler.toUserInfoResponse(user, roleCodes);
    }

    private List<String> getUserRoleCodes(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoles.stream()
            .map(ur -> {
                Role role = roleRepository.findById(ur.getRoleId());
                return role != null ? role.getRoleCode() : null;
            })
            .filter(code -> code != null)
            .collect(Collectors.toList());
    }
}
