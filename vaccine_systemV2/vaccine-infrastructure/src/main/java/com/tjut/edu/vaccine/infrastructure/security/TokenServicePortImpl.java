package com.tjut.edu.vaccine.infrastructure.security;

import com.tjut.edu.vaccine.domain.port.TokenServicePort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TokenServicePortImpl implements TokenServicePort {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenServicePortImpl(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String generateToken(Long userId, String phone, List<String> roles) {
        return jwtTokenProvider.generateToken(userId, phone, roles);
    }
}
