package com.agrotech.api.profile.domain.model.commands;

import java.util.Date;

public record CreateNotificationCommand(Long userId, String title, String message, Date sendAt) {
}
