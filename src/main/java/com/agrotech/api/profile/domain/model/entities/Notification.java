package com.agrotech.api.profile.domain.model.entities;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.profile.domain.model.commands.CreateNotificationCommand;
import lombok.Getter;

import java.util.Date;

@Getter
public class Notification {
    private Long id;
    private String title;
    private String message;
    private Date sendAt;
    private User user;

    public Notification(CreateNotificationCommand command, User user) {
        this.title = command.title();
        this.message = command.message();
        this.sendAt = command.sendAt();
        this.user = user;
    }

    public Notification(Long id, String title, String message, Date sendAt, User user) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.sendAt = sendAt;
        this.user = user;
    }

    public Long getUserId() {
        return user.getId();
    }
}
