package com.agrotech.api.ai.application.usecase;

import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationMatchDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationRequestDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationResponseDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationStatus;
import com.agrotech.api.ai.infrastructure.web.dto.AIResponseDto;
import com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repository.AvailableDateRepository;
import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.application.usecase.AdvisorService;
import com.agrotech.api.profile.application.usecase.ProfileService;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Profile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AIService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AIService.class);
    private static final Pattern ADVISOR_ID_PATTERN = Pattern.compile("(?im)^\\s*advisorId\\s*[:\\-]?\\s*(\\d+)\\s*$");
    private static final String INVALID_REQUEST_MESSAGE = "Necesito un poco mas de contexto para recomendarte un asesor. Indicame el tema, el problema que tienes y, si aplica, tu ubicacion.";
    private static final String INVALID_ADVISOR_MESSAGE = "Pude orientarte de forma general, pero no pude identificar un asesor valido. Intenta dar mas detalles para recomendarte uno correctamente.";
    private static final String GENERIC_FAILURE_MESSAGE = "No se pudo procesar tu solicitud en este momento. Intenta nuevamente mas tarde.";
    private static final int FINAL_MATCH_LIMIT = 3;
    private static final int SEMANTIC_CANDIDATE_LIMIT = 5;
    private static final double LOCATION_CITY_SCORE = 45.0;
    private static final double LOCATION_COUNTRY_SCORE = 25.0;
    private static final double MAX_RATING_SCORE = 20.0;
    private static final double MAX_EXPERIENCE_SCORE = 12.0;
    private static final int EXPERIENCE_YEARS_CAP = 12;
    private static final double MAX_AVAILABILITY_SCORE = 15.0;
    private static final double MAX_LEXICAL_SCORE = 4.0;
    private static final double MAX_SEMANTIC_SCORE = 8.0;
    private static final double READY_GAP_THRESHOLD = 6.0;

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    private final AvailableDateRepository availableDateRepository;
    private final ProfileService profileService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ObjectMapper objectMapper;
    private Client client;

    public AIService(
            AvailableDateRepository availableDateRepository,
            ProfileService profileService,
            AuthenticatedUserService authenticatedUserService,
            ObjectMapper objectMapper
    ) {
        this.availableDateRepository = availableDateRepository;
        this.profileService = profileService;
        this.authenticatedUserService = authenticatedUserService;
        this.objectMapper = objectMapper;
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

    public AIRecommendationResponseDto recommendAdvisors(AIRecommendationRequestDto request) {
        FarmerContext farmerContext = resolveFarmerContext(request);
        if (farmerContext.message().isBlank()) {
            return new AIRecommendationResponseDto(
                    AIRecommendationStatus.NEEDS_MORE_INFO,
                    null,
                    List.of(),
                    "Necesito un poco mas de contexto para sugerirte asesores.",
                    "Que tipo de problema agricola necesitas resolver y en que ubicacion te encuentras?",
                    null
            );
        }

        List<RankedAdvisorCandidate> rankedCandidates = buildRankedCandidates(farmerContext);
        if (rankedCandidates.isEmpty()) {
            return new AIRecommendationResponseDto(
                    AIRecommendationStatus.UNAVAILABLE,
                    null,
                    List.of(),
                    "No hay asesores disponibles con informacion suficiente para recomendarte una opcion ahora mismo.",
                    null,
                    null
            );
        }

        List<RankedAdvisorCandidate> topCandidates = rankedCandidates.stream()
                .limit(FINAL_MATCH_LIMIT)
                .toList();
        AIRecommendationStatus status = determineStatus(farmerContext.message(), rankedCandidates);
        Long selectedAdvisorId = status == AIRecommendationStatus.READY ? topCandidates.getFirst().advisor().getId() : null;
        String summary = buildSummary(farmerContext, topCandidates, status);
        String clarifyingQuestion = status == AIRecommendationStatus.NEEDS_MORE_INFO
                ? buildClarifyingQuestion(farmerContext, topCandidates)
                : null;
        String draftAppointmentMessage = status == AIRecommendationStatus.READY
                ? buildDraftAppointmentMessage(farmerContext, topCandidates.getFirst())
                : null;

        return new AIRecommendationResponseDto(
                status,
                selectedAdvisorId,
                topCandidates.stream()
                        .map(candidate -> toMatchDto(candidate, farmerContext))
                        .toList(),
                summary,
                clarifyingQuestion,
                draftAppointmentMessage
        );
    }

    private String generateContent(String prompt) {
        if (client == null) {
            return null;
        }

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            return response.text();
        } catch (Exception e) {
            LOGGER.warn("Error al procesar la solicitud a Gemini: {}", e.getMessage());
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

    private FarmerContext resolveFarmerContext(AIRecommendationRequestDto request) {
        String message = request == null || request.message() == null
                ? null
                : request.message().trim().replaceAll("\\s+", " ");
        String city = null;
        String country = null;

        try {
            AuthenticatedUser authenticatedUser = authenticatedUserService.getCurrentUser();
            if (authenticatedUser.role() == UserRole.FARMER) {
                Profile profile = profileService.getCurrentProfile();
                city = normalizeText(profile.getCity());
                country = normalizeText(profile.getCountry());
            }
        } catch (Exception exception) {
            LOGGER.debug("No se pudo completar el contexto del agricultor desde el perfil autenticado");
        }

        return new FarmerContext(message == null ? "" : message, city, country);
    }

    private List<RankedAdvisorCandidate> buildRankedCandidates(FarmerContext farmerContext) {
        Map<Long, LocalDate> nextAvailabilityByAdvisorId = availableDateRepository
                .findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscStartTimeAsc(
                        AvailableDateStatus.AVAILABLE,
                        LocalDate.now()
                ).stream()
                .collect(Collectors.toMap(
                        availableDate -> availableDate.getAdvisor().getId(),
                        availableDate -> availableDate.getScheduledDate(),
                        (firstDate, ignored) -> firstDate,
                        HashMap::new
                ));

        List<RankedAdvisorCandidate> deterministicCandidates = new ArrayList<>();
        for (Profile profile : profileService.getAdvisorProfiles()) {
            Advisor advisor = profileService.getAdvisorByUserId(profile.getUser().getId());
            double locationScore = computeLocationScore(farmerContext, profile);
            double ratingScore = computeRatingScore(advisor.getRating());
            double experienceScore = computeExperienceScore(profile.getExperience());
            double availabilityScore = computeAvailabilityScore(nextAvailabilityByAdvisorId.get(advisor.getId()));
            double lexicalScore = computeLexicalScore(farmerContext.message(), profile);

            deterministicCandidates.add(new RankedAdvisorCandidate(
                    advisor,
                    profile,
                    nextAvailabilityByAdvisorId.get(advisor.getId()),
                    locationScore + ratingScore + experienceScore + availabilityScore + lexicalScore,
                    0.0,
                    lexicalScore,
                    locationScore,
                    ratingScore,
                    experienceScore,
                    availabilityScore
            ));
        }

        List<RankedAdvisorCandidate> topForSemantic = deterministicCandidates.stream()
                .sorted(Comparator.comparingDouble(RankedAdvisorCandidate::finalScore).reversed())
                .limit(SEMANTIC_CANDIDATE_LIMIT)
                .toList();

        Map<Long, Double> semanticScoresByAdvisorId = scoreSemanticSimilarity(farmerContext.message(), topForSemantic);

        return deterministicCandidates.stream()
                .map(candidate -> {
                    double semanticScore = semanticScoresByAdvisorId.getOrDefault(candidate.advisor().getId(), 0.0);
                    return candidate.withSemanticScore(semanticScore);
                })
                .sorted(Comparator.comparingDouble(RankedAdvisorCandidate::finalScore).reversed()
                        .thenComparing(candidate -> candidate.advisor().getRating(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(candidate -> candidate.profile().getExperience(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(candidate -> candidate.nextAvailableDate(), Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private double computeLocationScore(FarmerContext farmerContext, Profile advisorProfile) {
        if (farmerContext.city() != null && advisorProfile.getCity() != null
                && normalizeText(advisorProfile.getCity()).equals(farmerContext.city())) {
            return LOCATION_CITY_SCORE;
        }
        if (farmerContext.country() != null && advisorProfile.getCountry() != null
                && normalizeText(advisorProfile.getCountry()).equals(farmerContext.country())) {
            return LOCATION_COUNTRY_SCORE;
        }
        return 0.0;
    }

    private double computeRatingScore(BigDecimal rating) {
        if (rating == null) {
            return 0.0;
        }
        return rating.doubleValue() * (MAX_RATING_SCORE / 5.0);
    }

    private double computeExperienceScore(Integer experience) {
        if (experience == null || experience <= 0) {
            return 0.0;
        }
        double normalizedExperience = Math.min(experience, EXPERIENCE_YEARS_CAP) / (double) EXPERIENCE_YEARS_CAP;
        return normalizedExperience * MAX_EXPERIENCE_SCORE;
    }

    private double computeAvailabilityScore(LocalDate nextAvailableDate) {
        if (nextAvailableDate == null) {
            return 0.0;
        }
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), nextAvailableDate);
        return Math.max(3.0, MAX_AVAILABILITY_SCORE - Math.min(daysUntil, 12));
    }

    private double computeLexicalScore(String userMessage, Profile profile) {
        if (userMessage == null || userMessage.isBlank()) {
            return 0.0;
        }

        Set<String> messageTerms = tokenize(userMessage);
        if (messageTerms.isEmpty()) {
            return 0.0;
        }

        Set<String> advisorTerms = tokenize(String.join(" ",
                profile.getOccupation() == null ? "" : profile.getOccupation(),
                profile.getDescription() == null ? "" : profile.getDescription()
        ));
        if (advisorTerms.isEmpty()) {
            return 0.0;
        }

        long matches = messageTerms.stream()
                .filter(advisorTerms::contains)
                .count();
        double ratio = (double) matches / messageTerms.size();
        return Math.min(MAX_LEXICAL_SCORE, ratio * MAX_LEXICAL_SCORE);
    }

    private Set<String> tokenize(String text) {
        return Pattern.compile("[^\\p{L}\\p{N}]+")
                .splitAsStream(normalizeText(text) == null ? "" : normalizeText(text))
                .filter(token -> token.length() >= 4)
                .collect(Collectors.toSet());
    }

    private Map<Long, Double> scoreSemanticSimilarity(String userMessage, List<RankedAdvisorCandidate> candidates) {
        if (client == null || userMessage == null || userMessage.isBlank() || candidates.isEmpty()) {
            return Map.of();
        }

        StringBuilder prompt = new StringBuilder("""
                Evalua la similitud semantica entre la consulta de un agricultor y los perfiles de asesores.
                Devuelve solo JSON valido con este formato:
                {"scores":[{"advisorId":1,"semanticScore":0.0}]}
                Usa semanticScore entre 0 y 8.
                No expliques nada fuera del JSON.
                
                Consulta:
                """).append(userMessage).append("\n\nAsesores:\n");

        for (RankedAdvisorCandidate candidate : candidates) {
            prompt.append("- advisorId: ").append(candidate.advisor().getId())
                    .append(", ocupacion: ").append(defaultText(candidate.profile().getOccupation()))
                    .append(", descripcion: ").append(defaultText(candidate.profile().getDescription()))
                    .append('\n');
        }

        JsonNode node = parseJsonResponse(generateContent(prompt.toString()));
        if (node == null || !node.has("scores") || !node.get("scores").isArray()) {
            return Map.of();
        }

        Map<Long, Double> scores = new HashMap<>();
        for (JsonNode scoreNode : node.get("scores")) {
            if (!scoreNode.hasNonNull("advisorId") || !scoreNode.hasNonNull("semanticScore")) {
                continue;
            }
            long advisorId = scoreNode.get("advisorId").asLong();
            double semanticScore = Math.max(0.0, Math.min(MAX_SEMANTIC_SCORE, scoreNode.get("semanticScore").asDouble()));
            scores.put(advisorId, semanticScore);
        }
        return scores;
    }

    private AIRecommendationStatus determineStatus(String userMessage, List<RankedAdvisorCandidate> rankedCandidates) {
        if (rankedCandidates.isEmpty()) {
            return AIRecommendationStatus.UNAVAILABLE;
        }
        if (isImproperRequest(userMessage)) {
            return AIRecommendationStatus.NEEDS_MORE_INFO;
        }
        if (rankedCandidates.size() == 1) {
            return AIRecommendationStatus.READY;
        }
        double gap = rankedCandidates.get(0).finalScore() - rankedCandidates.get(1).finalScore();
        return gap >= READY_GAP_THRESHOLD ? AIRecommendationStatus.READY : AIRecommendationStatus.NEEDS_MORE_INFO;
    }

    private AIRecommendationMatchDto toMatchDto(RankedAdvisorCandidate candidate, FarmerContext farmerContext) {
        return new AIRecommendationMatchDto(
                candidate.advisor().getId(),
                buildFullName(candidate.profile()),
                candidate.profile().getOccupation(),
                candidate.advisor().getRating(),
                candidate.profile().getExperience(),
                candidate.profile().getCity(),
                candidate.profile().getCountry(),
                candidate.nextAvailableDate(),
                buildWhy(candidate, farmerContext)
        );
    }

    private String buildWhy(RankedAdvisorCandidate candidate, FarmerContext farmerContext) {
        List<String> reasons = new ArrayList<>();
        if (candidate.locationScore() >= LOCATION_CITY_SCORE) {
            reasons.add("esta en tu misma ciudad");
        } else if (candidate.locationScore() >= LOCATION_COUNTRY_SCORE) {
            reasons.add("atiende en tu mismo pais");
        }
        if (candidate.semanticScore() > 0 || candidate.lexicalScore() > 0) {
            reasons.add("su perfil se alinea con tu necesidad");
        }
        if (candidate.advisor().getRating() != null) {
            reasons.add("tiene una calificacion de " + candidate.advisor().getRating().setScale(1, RoundingMode.HALF_UP));
        }
        if (candidate.profile().getExperience() != null && candidate.profile().getExperience() > 0) {
            reasons.add(candidate.profile().getExperience() + " anos de experiencia");
        }
        if (candidate.nextAvailableDate() != null) {
            reasons.add("tiene disponibilidad desde " + candidate.nextAvailableDate());
        }
        if (reasons.isEmpty() && farmerContext.message() != null && !farmerContext.message().isBlank()) {
            reasons.add("puede orientarte segun lo que describiste");
        }
        return String.join(", ", reasons) + ".";
    }

    private String buildSummary(FarmerContext farmerContext, List<RankedAdvisorCandidate> candidates, AIRecommendationStatus status) {
        JsonNode node = parseJsonResponse(generateContent(buildNarrativePrompt(
                farmerContext,
                candidates,
                status,
                "Devuelve solo JSON valido con este formato: {\"summary\":\"texto\",\"clarifyingQuestion\":\"texto o null\",\"draftAppointmentMessage\":\"texto o null\"}."
        )));
        if (node != null && node.hasNonNull("summary")) {
            return node.get("summary").asText();
        }
        RankedAdvisorCandidate firstCandidate = candidates.getFirst();
        return switch (status) {
            case READY -> "La mejor opcion es " + buildFullName(firstCandidate.profile()) + " por su cercania, experiencia y disponibilidad.";
            case NEEDS_MORE_INFO -> "Te muestro las opciones mas cercanas, pero necesito un poco mas de detalle para recomendarte una sola.";
            case UNAVAILABLE -> "No pude identificar una recomendacion util en este momento.";
        };
    }

    private String buildClarifyingQuestion(FarmerContext farmerContext, List<RankedAdvisorCandidate> candidates) {
        JsonNode node = parseJsonResponse(generateContent(buildNarrativePrompt(
                farmerContext,
                candidates,
                AIRecommendationStatus.NEEDS_MORE_INFO,
                "Devuelve solo JSON valido con este formato: {\"summary\":\"texto\",\"clarifyingQuestion\":\"texto o null\",\"draftAppointmentMessage\":null}."
        )));
        if (node != null && node.hasNonNull("clarifyingQuestion")) {
            return node.get("clarifyingQuestion").asText();
        }
        return "Que cultivo, problema especifico o etapa del proceso agricola necesitas atender?";
    }

    private String buildDraftAppointmentMessage(FarmerContext farmerContext, RankedAdvisorCandidate candidate) {
        JsonNode node = parseJsonResponse(generateContent(buildNarrativePrompt(
                farmerContext,
                List.of(candidate),
                AIRecommendationStatus.READY,
                "Devuelve solo JSON valido con este formato: {\"summary\":\"texto\",\"clarifyingQuestion\":null,\"draftAppointmentMessage\":\"texto\"}."
        )));
        if (node != null && node.hasNonNull("draftAppointmentMessage")) {
            return node.get("draftAppointmentMessage").asText();
        }
        return "Hola " + buildFullName(candidate.profile()) + ", necesito asesoria sobre " + farmerContext.message() + ". "
                + "Me gustaria coordinar una cita para revisar mi caso.";
    }

    private String buildNarrativePrompt(
            FarmerContext farmerContext,
            List<RankedAdvisorCandidate> candidates,
            AIRecommendationStatus status,
            String outputInstruction
    ) {
        StringBuilder prompt = new StringBuilder("""
                Eres AgroBot. Redacta respuestas cortas, claras y utiles para un agricultor.
                No uses markdown.
                """);
        prompt.append(outputInstruction).append('\n')
                .append("Estado: ").append(status.name()).append('\n')
                .append("Consulta: ").append(farmerContext.message()).append('\n')
                .append("Opciones:\n");

        if (farmerContext.city() != null || farmerContext.country() != null) {
            prompt.append("Ubicacion: ")
                    .append(defaultText(farmerContext.city()))
                    .append(", ")
                    .append(defaultText(farmerContext.country()))
                    .append('\n');
        }

        for (RankedAdvisorCandidate candidate : candidates) {
            prompt.append("- ")
                    .append(buildFullName(candidate.profile()))
                    .append(" | ocupacion: ").append(defaultText(candidate.profile().getOccupation()))
                    .append(" | experiencia: ").append(candidate.profile().getExperience() == null ? 0 : candidate.profile().getExperience())
                    .append(" | rating: ").append(candidate.advisor().getRating() == null ? "sin dato" : candidate.advisor().getRating())
                    .append(" | disponible desde: ").append(candidate.nextAvailableDate() == null ? "sin fecha" : candidate.nextAvailableDate())
                    .append('\n');
        }
        return prompt.toString();
    }

    private JsonNode parseJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }

        String trimmed = response.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim();
        int objectStart = trimmed.indexOf('{');
        int objectEnd = trimmed.lastIndexOf('}');
        if (objectStart < 0 || objectEnd <= objectStart) {
            return null;
        }

        try {
            return objectMapper.readTree(trimmed.substring(objectStart, objectEnd + 1));
        } catch (Exception exception) {
            LOGGER.debug("No se pudo parsear la respuesta JSON de Gemini");
            return null;
        }
    }

    private String buildFullName(Profile profile) {
        String firstName = profile.getFirstName() == null ? "" : profile.getFirstName().trim();
        String lastName = profile.getLastName() == null ? "" : profile.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? "Asesor disponible" : fullName;
    }

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "No especificado" : value.trim();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private record FarmerContext(
            String message,
            String city,
            String country
    ) {
    }

    private record RankedAdvisorCandidate(
            Advisor advisor,
            Profile profile,
            LocalDate nextAvailableDate,
            double baseScore,
            double semanticScore,
            double lexicalScore,
            double locationScore,
            double ratingScore,
            double experienceScore,
            double availabilityScore
    ) {
        private double finalScore() {
            return baseScore + semanticScore;
        }

        private RankedAdvisorCandidate withSemanticScore(double updatedSemanticScore) {
            return new RankedAdvisorCandidate(
                    advisor,
                    profile,
                    nextAvailableDate,
                    baseScore,
                    updatedSemanticScore,
                    lexicalScore,
                    locationScore,
                    ratingScore,
                    experienceScore,
                    availabilityScore
            );
        }
    }
}
