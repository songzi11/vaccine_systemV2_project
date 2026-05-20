package com.tjut.edu.vaccine.domain.port;

import java.util.List;

/**
 * 安全上下文端口 — 应用层通过此接口获取当前登录用户信息
 */
public interface SecurityContextPort {

    Long getCurrentUserId();

    List<String> getCurrentRoles();
}
