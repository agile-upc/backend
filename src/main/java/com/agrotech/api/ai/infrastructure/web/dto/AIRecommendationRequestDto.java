package com.agrotech.api.ai.infrastructure.web.dto;

public record AIRecommendationRequestDto(
        String message,
        String conversationId
) {
}
