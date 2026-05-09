package com.agrotech.api.appointment.domain.model.commands;

public record CreateReviewCommand(Long advisorId,
                                  Long farmerId,
                                  String comment,
                                  Integer rating) {
}
