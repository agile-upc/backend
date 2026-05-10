package com.agrotech.api.appointment.infrastructure.web.dto;

public record ReviewResource(Long id,
                             Long advisorId,
                             Long farmerId,
                             String comment,
                             Integer rating) {
}
