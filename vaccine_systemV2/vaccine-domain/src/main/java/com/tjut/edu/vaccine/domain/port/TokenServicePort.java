package com.tjut.edu.vaccine.domain.port;

import java.util.List;

/**
 * Token 服务端口 — 应用层通过此接口生成 JWT
 */
public interface TokenServicePort {

    String generateToken(Long userId, String phone, List<String> roles);
}
