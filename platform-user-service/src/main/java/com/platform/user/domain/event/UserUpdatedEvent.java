package com.platform.user.domain.event;

import com.platform.shared.domain.event.DomainEvent;

/**
 * 用户更新事件
 */
public class UserUpdatedEvent extends DomainEvent {

    public UserUpdatedEvent(Long aggregateId, Integer eventVersion, String tenantId) {
        super(aggregateId, eventVersion, tenantId);
    }
}