package com.platform.user.domain.event;

import com.platform.shared.domain.event.DomainEvent;

/**
 * 用户删除事件
 */
public class UserDeletedEvent extends DomainEvent {

    public UserDeletedEvent(Long aggregateId, Integer eventVersion, String tenantId) {
        super(aggregateId, eventVersion, tenantId);
    }
}