package com.daily.dailychineseculture.event;

import org.springframework.context.ApplicationEvent;

public class CampProgressUpdateEvent extends ApplicationEvent {
    private final Long userId;
    private final Integer campId;

    public CampProgressUpdateEvent(Object source, Long userId, Integer campId) {
        super(source);
        this.userId = userId;
        this.campId = campId;
    }

    public Long getUserId() { return userId; }
    public Integer getCampId() { return campId; }
}