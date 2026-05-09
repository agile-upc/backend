package com.agrotech.api.profile.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.iam.infrastructure.persistence.jpa.mappers.UserMapper;
import com.agrotech.api.profile.domain.model.entities.Notification;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.NotificationEntity;

public class NotificationMapper {
    public static Notification toDomain(NotificationEntity entity) {
        if (entity == null) return null;
        return new Notification(
                entity.getId(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getSendAt(),
                UserMapper.toDomain(entity.getUser())
        );
    }

    public static NotificationEntity toEntity(Notification domain) {
        if (domain == null) return null;
        return NotificationEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .message(domain.getMessage())
                .sendAt(domain.getSendAt())
                .user(UserMapper.toEntity(domain.getUser()))
                .build();
    }
}
