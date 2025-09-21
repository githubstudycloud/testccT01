package com.platform.user.infrastructure.repository;

import com.platform.user.domain.User;
import com.platform.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户JPA仓储实现
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsernameAndDeletedFalse(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmailAndDeletedFalse(String email);

    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhoneAndDeletedFalse(String phone);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsernameAndDeletedFalse(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmailAndDeletedFalse(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhoneAndDeletedFalse(String phone);

    /**
     * 查找激活用户列表
     */
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deleted = false")
    List<User> findByStatusAndDeletedFalse(@Param("status") UserStatus status);

    /**
     * 根据租户ID查找用户
     */
    List<User> findByTenantIdAndDeletedFalse(String tenantId);
}