package com.agrotech.api.profile.application.mapper;

import com.agrotech.api.iam.domain.model.User;
import com.agrotech.api.profile.infrastructure.web.dto.NotificationResource;
import com.agrotech.api.profile.domain.model.Notification;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class NotificationMapper {
    public NotificationResource toNotificationResource(Notification notification) {
        return new NotificationResource(
                notification.getId(),
                notification.getUser().getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getSendAt()
        );
    }

    public Notification toNotification(User user, String title, String message, Date sendAt) {
        return Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .sendAt(sendAt)
                .build();
    }
}
