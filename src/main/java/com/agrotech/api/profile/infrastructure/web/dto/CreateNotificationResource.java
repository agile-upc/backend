package com.agrotech.api.profile.infrastructure.web.dto;

import java.util.Date;

public record CreateNotificationResource(Long userId,
                                         String title,
                                         String message,
                                         Date sendAt) {
}
