package com.agrotech.api.appointment.application.internal.outboundservices.acl;

import com.agrotech.api.profile.interfaces.acl.NotificationsContextFacade;
import com.agrotech.api.profile.interfaces.rest.resources.CreateNotificationResource;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ExternalNotificationsService {
    private final NotificationsContextFacade notificationsContextFacade;

    public ExternalNotificationsService(NotificationsContextFacade notificationsContextFacade) {
        this.notificationsContextFacade = notificationsContextFacade;
    }

    public Long createNotification(Long userId, String title, String message, Date sendAt) {
        return notificationsContextFacade.createNotification(new CreateNotificationResource(userId, title, message, sendAt));
    }
}
