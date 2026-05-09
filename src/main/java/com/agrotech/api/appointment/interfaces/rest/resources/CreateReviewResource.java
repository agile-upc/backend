package com.agrotech.api.appointment.interfaces.rest.resources;

public record CreateReviewResource(Long advisorId,
                                   Long farmerId,
                                   String comment,
                                   Integer rating) {
}
