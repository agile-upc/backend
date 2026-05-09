package com.agrotech.api.profile.domain.services;

import com.agrotech.api.profile.domain.model.entities.Notification;
import com.agrotech.api.profile.domain.model.queries.GetAllNotificationsQuery;
import com.agrotech.api.profile.domain.model.queries.GetNotificationByIdQuery;
import com.agrotech.api.profile.domain.model.queries.GetNotificationsByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface NotificationQueryService {
    Optional<Notification> handle(GetNotificationByIdQuery query);
    List<Notification> handle(GetAllNotificationsQuery query);
    List<Notification> handle(GetNotificationsByUserIdQuery query);
}
