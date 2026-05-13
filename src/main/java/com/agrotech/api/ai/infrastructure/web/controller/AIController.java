package com.agrotech.api.ai.infrastructure.web.controller;

import com.agrotech.api.ai.application.usecase.AIService;
import com.agrotech.api.ai.infrastructure.web.dto.AIRequestDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIResponseDto;
import com.agrotech.api.profile.application.usecase.AdvisorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/ai", produces = APPLICATION_JSON_VALUE)
@Tag(name = "AgroBot", description = "AgroBot Endpoints")
public class AIController {
    private final AIService aiService;
    private final AdvisorService advisorService;

    public AIController(AIService aiService, AdvisorService advisorService) {
        this.aiService = aiService;
        this.advisorService = advisorService;
    }

    @Operation(summary = "Chat con AgroBot")
    @PostMapping("/chat")
    public ResponseEntity<AIResponseDto> chat(@RequestBody AIRequestDto request) {
        return ResponseEntity.ok(
                aiService.recommendAdvisor(
                        request.message(),
                        advisorService.getAdvisorRecommendationOptions()
                )
        );
    }
}
