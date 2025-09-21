package com.platform.user.domain.event;

import com.platform.shared.domain.event.DomainEvent;

/**
 * 用户创建事件
 */
public class UserCreatedEvent extends DomainEvent {

    public UserCreatedEvent(Long aggregateId, Integer eventVersion, String tenantId) {
        super(aggregateId, eventVersion, tenantId);
    }
}