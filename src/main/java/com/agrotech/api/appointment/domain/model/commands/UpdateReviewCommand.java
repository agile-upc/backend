package com.agrotech.api.appointment.domain.model.commands;

public record UpdateReviewCommand(Long id, String comment, Integer rating) {
}
