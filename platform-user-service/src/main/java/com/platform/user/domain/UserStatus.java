package com.platform.user.domain;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    /**
     * 激活状态
     */
    ACTIVE,

    /**
     * 禁用状态
     */
    DISABLED,

    /**
     * 锁定状态
     */
    LOCKED,

    /**
     * 待激活状态
     */
    PENDING_ACTIVATION
}