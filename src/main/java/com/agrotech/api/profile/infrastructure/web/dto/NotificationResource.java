package com.agrotech.api.profile.infrastructure.web.dto;

import java.util.Date;

public record NotificationResource(Long id,
                                   Long userId,
                                   String title,
                                   String message,
                                   Date sendAt) {
}
