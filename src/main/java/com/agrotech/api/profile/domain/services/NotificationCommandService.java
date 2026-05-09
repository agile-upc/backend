package com.agrotech.api.profile.domain.services;

import com.agrotech.api.profile.domain.model.commands.CreateNotificationCommand;
import com.agrotech.api.profile.domain.model.commands.DeleteNotificationCommand;

public interface NotificationCommandService {
    Long handle(CreateNotificationCommand command);
    void handle(DeleteNotificationCommand command);
}
