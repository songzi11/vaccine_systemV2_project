package com.tjut.edu.vaccine.infrastructure.security;

import com.tjut.edu.vaccine.domain.port.SecurityContextPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityContextPortImpl implements SecurityContextPort {

    @Override
    public Long getCurrentUserId() {
        return UserContext.getUserId();
    }

    @Override
    public List<String> getCurrentRoles() {
        return UserContext.getRoles();
    }
}
