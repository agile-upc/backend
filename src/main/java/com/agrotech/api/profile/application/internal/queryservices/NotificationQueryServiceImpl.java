package com.agrotech.api.profile.application.internal.queryservices;

import com.agrotech.api.profile.domain.model.entities.Notification;
import com.agrotech.api.profile.domain.model.queries.GetAllNotificationsQuery;
import com.agrotech.api.profile.domain.model.queries.GetNotificationByIdQuery;
import com.agrotech.api.profile.domain.model.queries.GetNotificationsByUserIdQuery;
import com.agrotech.api.profile.domain.services.NotificationQueryService;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.NotificationMapper;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationQueryServiceImpl implements NotificationQueryService {
    private final NotificationRepository notificationRepository;

    public NotificationQueryServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Optional<Notification> handle(GetNotificationByIdQuery query) {
        return notificationRepository.findById(query.id())
                .map(NotificationMapper::toDomain);
    }

    @Override
    public List<Notification> handle(GetAllNotificationsQuery query) {
        return notificationRepository.findAll()
                .stream()
                .map(NotificationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> handle(GetNotificationsByUserIdQuery query) {
        return notificationRepository.findByUser_Id(query.userId())
                .stream()
                .map(NotificationMapper::toDomain)
                .collect(Collectors.toList());
    }
}
