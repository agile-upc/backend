package com.agrotech.api.ai.infrastructure.web.controller;

import com.agrotech.api.ai.infrastructure.web.dto.AIRequestDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIResponseDto;
import com.agrotech.api.ai.application.usecase.AIService;
import com.agrotech.api.profile.domain.model.Profile;
import com.agrotech.api.profile.application.usecase.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/ai", produces = APPLICATION_JSON_VALUE)
@Tag(name = "AgroBot", description = "AgroBot Endpoints")
public class AIController {
    private final AIService aiService;
    private final ProfileService profileService;

    public AIController(AIService aiService, ProfileService profileService) {
        this.aiService = aiService;
        this.aiService.instanceGemini();
        this.profileService = profileService;
    }

    @Operation(summary = "Chat con AgroBot")
    @PostMapping("/chat")
    public ResponseEntity<AIResponseDto> chat(@RequestBody AIRequestDto request) {
        var profiles = profileService.getAdvisorProfiles();
        StringBuilder advisors = new StringBuilder("Sugiere el asesor mas adecuado a partir del listado de los asesores:\n");
        for (Profile profile : profiles) {
            advisors.append("- ")
                    .append(profile.getUser().getId())
                    .append(" ")
                    .append(profile.getFirstName())
                    .append(" ")
                    .append(profile.getLastName())
                    .append(", ocupacion: ")
                    .append(profile.getOccupation())
                    .append(".\n");
        }

        String optimizedPrompt = advisors + "Consulta del usuario: " + request.message() + "\nAdemas, respalda brevemente tu eleccion.";
        String response = aiService.generateContent(optimizedPrompt);

        Long advisorId = null;
        String message = response == null ? "" : response;

        if (response != null) {
            Pattern pattern = Pattern.compile("(\\d+)\\s*$");
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                try {
                    long userId = Long.parseLong(matcher.group(1));
                    advisorId = profileService.getAdvisorByUserId(userId).getId();
                    int startOfNumber = matcher.start(1);
                    message = response.substring(0, startOfNumber).replaceAll("(?i)(userId\\s*[:\\-\\s]*)?$", "").trim();
                } catch (RuntimeException ignored) {
                    message = response;
                }
            }
        }

        return new ResponseEntity<>(new AIResponseDto(message, advisorId), HttpStatus.OK);
    }
}
