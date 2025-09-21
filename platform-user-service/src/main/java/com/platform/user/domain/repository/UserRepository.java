package com.platform.user.domain.repository;

import com.platform.shared.repository.Repository;
import com.platform.user.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface UserRepository extends Repository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 查找激活用户列表
     */
    List<User> findActiveUsers();

    /**
     * 根据租户ID查找用户
     */
    List<User> findByTenantId(String tenantId);
}