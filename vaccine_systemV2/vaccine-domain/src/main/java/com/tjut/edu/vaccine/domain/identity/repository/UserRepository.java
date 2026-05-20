package com.tjut.edu.vaccine.domain.identity.repository;

import com.tjut.edu.vaccine.domain.identity.aggregate.User;

import java.util.List;

/**
 * 用户仓储接口
 */
public interface UserRepository {

    User findById(Long id);

    List<User> findAll();

    List<User> findByKeyword(String keyword);

    User findByPhone(String phone);

    User findByUsername(String username);

    void save(User user);

    void updateStatus(User user);

    void incrementNoShowCount(Long userId);

    void freezeForNoShow(Long userId, int days);
}
