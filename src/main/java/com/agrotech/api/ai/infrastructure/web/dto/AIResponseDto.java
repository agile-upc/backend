package com.agrotech.api.ai.infrastructure.web.dto;

public record AIResponseDto(
        String response,
        Long advisorId
) {
}
