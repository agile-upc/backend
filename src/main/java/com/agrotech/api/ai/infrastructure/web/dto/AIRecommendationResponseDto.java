package com.agrotech.api.ai.infrastructure.web.dto;

import java.util.List;

public record AIRecommendationResponseDto(
        AIRecommendationStatus status,
        Long selectedAdvisorId,
        List<AIRecommendationMatchDto> matches,
        String summary,
        String clarifyingQuestion,
        String draftAppointmentMessage,
        String conversationId,
        int questionsAsked,
        int maxQuestions,
        boolean usedFallback
) {
}
