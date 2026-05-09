package com.agrotech.api.ai.controller;

import com.agrotech.api.ai.dto.AIRequestDto;
import com.agrotech.api.ai.dto.AIResponseDto;
import com.agrotech.api.ai.service.AIService;
import com.agrotech.api.profile.domain.model.aggregates.Profile;
import com.agrotech.api.profile.domain.model.queries.GetAdvisorByUserIdQuery;
import com.agrotech.api.profile.domain.model.queries.GetAllAdvisorProfilesQuery;
import com.agrotech.api.profile.domain.services.AdvisorQueryService;
import com.agrotech.api.profile.domain.services.ProfileQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RequestMapping(value = "api/v1/ai", produces = APPLICATION_JSON_VALUE)
@Tag(name = "AgroBot", description = "AgroBot Endpoints")
public class AIController {
    private final AIService aiService;
    private final ProfileQueryService profileQueryService;
    private final AdvisorQueryService advisorQueryService;


    public AIController(AIService aiService, ProfileQueryService profileQueryService, AdvisorQueryService advisorQueryService) {
        this.aiService = aiService;
        this.aiService.instanceGemini();
        this.profileQueryService = profileQueryService;
        this.advisorQueryService = advisorQueryService;
    }

    @Operation(summary = "Chat con AgroBot")
    @RequestMapping(value = "/chat", method = RequestMethod.POST)
    public ResponseEntity<AIResponseDto> chat(@RequestBody AIRequestDto request) {
        var profiles = profileQueryService.handle(new GetAllAdvisorProfilesQuery());
        StringBuilder advisors = new StringBuilder("Sugiere el asesor más adecuado a partir del listado de los asesores:\n");
        for (Profile profile : profiles) {
            advisors.append("- ").append(profile.getUserId()).append(" ").append(profile.getFirstName()).append(" ").append(profile.getLastName()).append(", ocupación: ").append(profile.getOccupation()).append(".\n");
        }
        var optimizedPrompt = advisors + "Consulta del usuario: " + request.message() + "\nAdemás, respalda brevemente su elección de por qué elegiste a ese";
        String response = aiService.generateContent(optimizedPrompt);

        Long advisorId = null;
        String message = response == null ? "" : response;

        // Busca el último número al final del texto (acepta formatos como "userId: 3", "3", etc.)
        if (response != null) {
            Pattern pattern = Pattern.compile("(\\d+)\\s*$");
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                try {
                    long userId = Long.parseLong(matcher.group(1));
                    var advisor = advisorQueryService.handle(new GetAdvisorByUserIdQuery(userId));
                    if (advisor != null && advisor.isPresent()) {
                        advisorId = advisor.get().getId();
                    }
                    int startOfNumber = matcher.start(1);
                    String withoutId = response.substring(0, startOfNumber);
                    withoutId = withoutId.replaceAll("(?i)(userId\\s*[:\\-\\s]*)?$", "").trim();
                    message = withoutId;
                } catch (NumberFormatException e) {
                    // no es un id numérico válido, mantener message como response y advisorId en null
                }
            }
        }

        AIResponseDto aiResponseDto = new AIResponseDto(message, advisorId);
        return new ResponseEntity<>(aiResponseDto, HttpStatus.OK);
    }

}
