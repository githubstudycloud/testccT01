package com.platform.user.domain;

import com.platform.shared.domain.AggregateRoot;
import com.platform.shared.domain.valueobject.Email;
import com.platform.shared.domain.valueobject.Phone;
import com.platform.user.domain.event.UserCreatedEvent;
import com.platform.user.domain.event.UserDeletedEvent;
import com.platform.user.domain.event.UserUpdatedEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户聚合根
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends AggregateRoot {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "avatar", length = 200)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    private User(String username, String password, String email, String phone, String nickname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.nickname = nickname;
        this.status = UserStatus.ACTIVE;

        // 发布用户创建事件
        addDomainEvent(new UserCreatedEvent(this.getId(), this.getVersion(), this.getTenantId()));
    }

    /**
     * 创建用户
     */
    public static User create(String username, String password, String email, String phone, String nickname) {
        validateUsername(username);
        validatePassword(password);
        if (email != null) {
            new Email(email); // 验证邮箱格式
        }
        if (phone != null) {
            new Phone(phone); // 验证手机号格式
        }

        return new User(username, password, email, phone, nickname);
    }

    /**
     * 更新用户信息
     */
    public void updateProfile(String email, String phone, String nickname, String avatar) {
        if (email != null) {
            new Email(email); // 验证邮箱格式
            this.email = email;
        }
        if (phone != null) {
            new Phone(phone); // 验证手机号格式
            this.phone = phone;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (avatar != null) {
            this.avatar = avatar;
        }

        // 发布用户更新事件
        addDomainEvent(new UserUpdatedEvent(this.getId(), this.getVersion(), this.getTenantId()));
    }

    /**
     * 修改密码
     */
    public void changePassword(String newPassword) {
        validatePassword(newPassword);
        this.password = newPassword;

        // 发布用户更新事件
        addDomainEvent(new UserUpdatedEvent(this.getId(), this.getVersion(), this.getTenantId()));
    }

    /**
     * 激活用户
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
        addDomainEvent(new UserUpdatedEvent(this.getId(), this.getVersion(), this.getTenantId()));
    }

    /**
     * 禁用用户
     */
    public void disable() {
        this.status = UserStatus.DISABLED;
        addDomainEvent(new UserUpdatedEvent(this.getId(), this.getVersion(), this.getTenantId()));
    }

    /**
     * 记录登录时间
     */
    public void recordLogin() {
        this.lastLoginTime = LocalDateTime.now();
    }

    /**
     * 删除用户（软删除）
     */
    public void delete() {
        markAsDeleted();
        addDomainEvent(new UserDeletedEvent(this.getId(), this.getVersion(), this.getTenantId()));
    }

    /**
     * 检查用户是否激活
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status) && !isDeleted();
    }

    private static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("用户名长度必须在3-50字符之间");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
    }
}