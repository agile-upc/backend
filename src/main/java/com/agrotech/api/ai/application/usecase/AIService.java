package com.agrotech.api.ai.application.usecase;

import com.agrotech.api.ai.infrastructure.web.dto.AIResponseDto;
import com.agrotech.api.profile.application.usecase.AdvisorService;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AIService {
    private static final Pattern ADVISOR_ID_PATTERN = Pattern.compile("(?im)^\\s*advisorId\\s*[:\\-]?\\s*(\\d+)\\s*$");
    private static final String INVALID_REQUEST_MESSAGE = "Necesito un poco mas de contexto para recomendarte un asesor. Indicame el tema, el problema que tienes y, si aplica, tu ubicacion.";
    private static final String INVALID_ADVISOR_MESSAGE = "Pude orientarte de forma general, pero no pude identificar un asesor valido. Intenta dar mas detalles para recomendarte uno correctamente.";
    private static final String GENERIC_FAILURE_MESSAGE = "No se pudo procesar tu solicitud en este momento. Intenta nuevamente mas tarde.";

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    private Client client;

    AIService() {
    }

    @PostConstruct
    public void instanceGemini() {
        if (apiKey == null || apiKey.isBlank()) {
            return;
        }

        client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public AIResponseDto recommendAdvisor(String userMessage, List<AdvisorService.AdvisorRecommendationOption> advisors) {
        if (isImproperRequest(userMessage)) {
            return new AIResponseDto(INVALID_REQUEST_MESSAGE, null);
        }

        if (advisors == null || advisors.isEmpty()) {
            return new AIResponseDto("No hay asesores disponibles en este momento.", null);
        }

        String response = generateContent(buildPrompt(userMessage, advisors));
        if (response == null || response.isBlank()) {
            return new AIResponseDto(GENERIC_FAILURE_MESSAGE, null);
        }

        return buildRecommendation(response, advisors);
    }

    private String generateContent(String prompt) {
        if (client == null) {
            return null;
        }

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al procesar la solicitud a Gemini: " + e.getMessage());
            return null;
        }
    }

    private String buildPrompt(String userMessage, List<AdvisorService.AdvisorRecommendationOption> advisors) {
        StringBuilder advisorList = new StringBuilder("Eres AgroBot y recomiendas un asesor de AgroTech.\n");
        advisorList.append("Elige solo un asesor del siguiente listado:\n");

        for (AdvisorService.AdvisorRecommendationOption advisor : advisors) {
            advisorList.append("- advisorId: ")
                    .append(advisor.advisorId())
                    .append(", nombre: ")
                    .append(advisor.fullName())
                    .append(", ocupacion: ")
                    .append(advisor.occupation() == null ? "No especificada" : advisor.occupation())
                    .append(".\n");
        }

        advisorList.append("Consulta del usuario: ")
                .append(userMessage.trim())
                .append("\n")
                .append("Responde en texto plano, sin markdown y de forma breve.\n")
                .append("Si la consulta no tiene suficiente contexto, pide mas detalles y no inventes especialidades.\n")
                .append("La ultima linea debe ser exactamente con este formato: advisorId: <numero>.\n")
                .append("Si no puedes recomendar un asesor valido del listado, la ultima linea debe ser: advisorId: none");

        return advisorList.toString();
    }

    private AIResponseDto buildRecommendation(String response, List<AdvisorService.AdvisorRecommendationOption> advisors) {
        String message = response.trim();
        Matcher matcher = ADVISOR_ID_PATTERN.matcher(response);
        if (!matcher.find()) {
            message = sanitizeMessage(message);
            return new AIResponseDto(
                    message.isBlank() ? INVALID_ADVISOR_MESSAGE : message + " " + INVALID_ADVISOR_MESSAGE,
                    null
            );
        }

        Long advisorId = Long.parseLong(matcher.group(1));
        Set<Long> validAdvisorIds = advisors.stream()
                .map(AdvisorService.AdvisorRecommendationOption::advisorId)
                .collect(Collectors.toSet());

        message = sanitizeMessage(response.substring(0, matcher.start()));
        if (!validAdvisorIds.contains(advisorId)) {
            return new AIResponseDto(
                    message.isBlank() ? INVALID_ADVISOR_MESSAGE : message + " " + INVALID_ADVISOR_MESSAGE,
                    null
            );
        }

        if (message.isBlank()) {
            message = "Te recomiendo un asesor segun lo que me comentaste.";
        }

        return new AIResponseDto(message, advisorId);
    }

    private boolean isImproperRequest(String userMessage) {
        if (userMessage == null) {
            return true;
        }

        String normalized = userMessage.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            return true;
        }

        if (!normalized.matches(".*[\\p{L}\\p{N}].*")) {
            return true;
        }

        String[] words = normalized.split("\\s+");
        return normalized.length() < 12 && words.length < 2;
    }

    private String sanitizeMessage(String message) {
        return message == null ? "" : message
                .replaceAll("(?im)^\\s*advisorId\\s*[:\\-]?.*$", "")
                .trim();
    }
}
