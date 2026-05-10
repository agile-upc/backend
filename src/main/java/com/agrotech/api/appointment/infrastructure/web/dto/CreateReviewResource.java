package com.agrotech.api.appointment.infrastructure.web.dto;

public record CreateReviewResource(Long advisorId,
                                   String comment,
                                   Integer rating) {
}
