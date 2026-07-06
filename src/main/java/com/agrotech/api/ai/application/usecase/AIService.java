package com.agrotech.api.ai.application.usecase;

import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationMatchDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationRequestDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationResponseDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationStatus;
import com.agrotech.api.ai.infrastructure.web.dto.AIResponseDto;
import com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus;
import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.application.usecase.AdvisorService;
import com.agrotech.api.profile.application.usecase.ProfileService;
import com.agrotech.api.profile.domain.model.Profile;
import com.agrotech.api.profile.infrastructure.persistence.jpa.projection.AdvisorRecommendationProjection;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.AdvisorRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AIService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AIService.class);
    private static final Pattern ADVISOR_ID_PATTERN = Pattern.compile("(?im)^\\s*advisorId\\s*[:\\-]?\\s*(\\d+)\\s*$");
    private static final Pattern TOKEN_SPLITTER = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final String DEFAULT_VERTEX_LOCATION = "global";
    private static final String DEFAULT_MODEL_ID = "gemini-2.5-flash";
    private static final String CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    private static final String INVALID_REQUEST_MESSAGE = "Necesito un poco mas de contexto para recomendarte un asesor. Indicame el tema, el problema que tienes y, si aplica, tu ubicacion.";
    private static final String INVALID_ADVISOR_MESSAGE = "Pude orientarte de forma general, pero no pude identificar un asesor valido. Intenta dar mas detalles para recomendarte uno correctamente.";
    private static final String GENERIC_FAILURE_MESSAGE = "No se pudo procesar tu solicitud en este momento. Intenta nuevamente mas tarde.";
    private static final int FINAL_MATCH_LIMIT = 3;
    private static final int SEMANTIC_CANDIDATE_LIMIT = 5;
    private static final int MAX_CLARIFYING_QUESTIONS = 1;
    private static final Duration DEFAULT_SESSION_TTL = Duration.ofMinutes(15);
    private static final DateTimeFormatter HUMAN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "PE"));
    private static final double LOCATION_CITY_SCORE = 45.0;
    private static final double LOCATION_COUNTRY_SCORE = 25.0;
    private static final double MAX_RATING_SCORE = 20.0;
    private static final double MAX_EXPERIENCE_SCORE = 12.0;
    private static final int EXPERIENCE_YEARS_CAP = 12;
    private static final double MAX_AVAILABILITY_SCORE = 15.0;
    private static final double MAX_LEXICAL_SCORE = 4.0;
    private static final double MAX_SEMANTIC_SCORE = 8.0;
    private static final double READY_GAP_THRESHOLD = 6.0;

    @Value("${VERTEX_AI_PROJECT_ID:${GCS_PROJECT_ID:}}")
    private String vertexAiProjectId;

    @Value("${VERTEX_AI_LOCATION:" + DEFAULT_VERTEX_LOCATION + "}")
    private String vertexAiLocation;

    @Value("${VERTEX_AI_MODEL_ID:" + DEFAULT_MODEL_ID + "}")
    private String vertexAiModelId;

    private final AdvisorRepository advisorRepository;
    private final ProfileService profileService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Duration sessionTtl;
    private final GeminiGateway geminiGateway;
    private final Map<String, RecommendationSession> recommendationSessions = new ConcurrentHashMap<>();
    private HttpClient httpClient;
    private GoogleCredentials googleCredentials;

    @Autowired
    public AIService(
            AdvisorRepository advisorRepository,
            ProfileService profileService,
            AuthenticatedUserService authenticatedUserService,
            ObjectMapper objectMapper
    ) {
        this(
                advisorRepository,
                profileService,
                authenticatedUserService,
                objectMapper,
                Clock.systemDefaultZone(),
                DEFAULT_SESSION_TTL,
                null
        );
    }

    AIService(
            AdvisorRepository advisorRepository,
            ProfileService profileService,
            AuthenticatedUserService authenticatedUserService,
            ObjectMapper objectMapper,
            Clock clock,
            Duration sessionTtl
    ) {
        this(
                advisorRepository,
                profileService,
                authenticatedUserService,
                objectMapper,
                clock,
                sessionTtl,
                null
        );
    }

    AIService(
            AdvisorRepository advisorRepository,
            ProfileService profileService,
            AuthenticatedUserService authenticatedUserService,
            ObjectMapper objectMapper,
            Clock clock,
            Duration sessionTtl,
            GeminiGateway geminiGateway
    ) {
        this.advisorRepository = advisorRepository;
        this.profileService = profileService;
        this.authenticatedUserService = authenticatedUserService;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.sessionTtl = sessionTtl;
        this.geminiGateway = geminiGateway == null ? this::requestGeminiContent : geminiGateway;
    }

    @PostConstruct
    public void instanceGemini() {
        if (vertexAiProjectId == null || vertexAiProjectId.isBlank()) {
            LOGGER.warn("Vertex AI is disabled because VERTEX_AI_PROJECT_ID is not configured");
            return;
        }

        try {
            googleCredentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(List.of(CLOUD_PLATFORM_SCOPE));
            httpClient = HttpClient.newHttpClient();
            LOGGER.info(
                    "Vertex AI initialized [projectId={}, location={}, modelId={}]",
                    vertexAiProjectId,
                    vertexAiLocation,
                    vertexAiModelId
            );
        } catch (IOException exception) {
            LOGGER.warn("No se pudo inicializar Vertex AI con Application Default Credentials: {}", exception.getMessage());
            googleCredentials = null;
            httpClient = null;
        }
    }

    public AIResponseDto recommendAdvisor(String userMessage, List<AdvisorService.AdvisorRecommendationOption> advisors) {
        if (isImproperRequest(userMessage)) {
            return new AIResponseDto(INVALID_REQUEST_MESSAGE, null);
        }

        if (advisors == null || advisors.isEmpty()) {
            return new AIResponseDto("No hay asesores disponibles en este momento.", null);
        }

        GeminiTextResult response = generateContent(buildPrompt(userMessage, advisors), "chat.recommend");
        if (response.text() == null || response.text().isBlank()) {
            return new AIResponseDto(GENERIC_FAILURE_MESSAGE, null);
        }

        return buildRecommendation(response.text(), advisors);
    }

    public AIRecommendationResponseDto recommendAdvisors(AIRecommendationRequestDto request) {
        RecommendationSession activeSession = getActiveSession(normalizeConversationId(request == null ? null : request.conversationId()));
        if (activeSession != null) {
            return continueRecommendationSession(activeSession, request);
        }
        return startRecommendationSession(request);
    }

    private AIRecommendationResponseDto startRecommendationSession(AIRecommendationRequestDto request) {
        FarmerContext farmerContext = resolveFarmerContext(request == null ? null : request.message());
        boolean shouldAttemptGemini = shouldAttemptGemini(farmerContext.message());
        if (!shouldAttemptGemini) {
            logFallback("recommendations.start", "Gemini skipped because the message was blank or too vague", farmerContext.message());
        }
        RankingResult rankingResult = buildRankedCandidates(farmerContext, shouldAttemptGemini);

        if (rankingResult.candidates().isEmpty()) {
            return buildResponse(
                    AIRecommendationStatus.UNAVAILABLE,
                    null,
                    List.of(),
                    "No hay asesores disponibles con información suficiente para recomendarte una opción ahora mismo.",
                    null,
                    null,
                    null,
                    0,
                    true
            );
        }

        List<RankedAdvisorCandidate> topCandidates = rankingResult.candidates().stream()
                .limit(FINAL_MATCH_LIMIT)
                .toList();
        AIRecommendationStatus status = determineStatus(farmerContext.message(), rankingResult.candidates());

        if (status == AIRecommendationStatus.NEEDS_MORE_INFO) {
            NarrativeContent narrative = buildNeedsMoreInfoNarrative(farmerContext, topCandidates, shouldAttemptGemini);
            String conversationId = UUID.randomUUID().toString();
            recommendationSessions.put(
                    conversationId,
                    new RecommendationSession(
                            conversationId,
                            farmerContext,
                            topCandidates,
                            1,
                            rankingResult.usedFallback() || narrative.usedFallback(),
                            Instant.now(clock).plus(sessionTtl)
                    )
            );
            return buildResponse(
                    AIRecommendationStatus.NEEDS_MORE_INFO,
                    null,
                    topCandidates,
                    narrative.summary(),
                    narrative.clarifyingQuestion(),
                    null,
                    conversationId,
                    1,
                    rankingResult.usedFallback() || narrative.usedFallback()
            );
        }

        RankedAdvisorCandidate bestCandidate = topCandidates.getFirst();
        NarrativeContent narrative = buildReadyNarrative(farmerContext, bestCandidate, topCandidates, shouldAttemptGemini);
        return buildResponse(
                AIRecommendationStatus.READY,
                bestCandidate.advisorId(),
                topCandidates,
                narrative.summary(),
                null,
                narrative.draftAppointmentMessage(),
                null,
                0,
                rankingResult.usedFallback() || narrative.usedFallback()
        );
    }

    private AIRecommendationResponseDto continueRecommendationSession(RecommendationSession session, AIRecommendationRequestDto request) {
        recommendationSessions.remove(session.conversationId());

        FarmerContext mergedContext = session.farmerContext().withMessage(
                mergeMessages(session.farmerContext().message(), request == null ? null : request.message())
        );
        boolean shouldAttemptGemini = shouldAttemptGemini(mergedContext.message());
        LOGGER.info(
                "Recommendation session using cached shortlist for final decision [conversationId={}, questionsAsked={}, geminiEnabled={}]",
                session.conversationId(),
                session.questionsAsked(),
                shouldAttemptGemini
        );
        List<RankedAdvisorCandidate> topCandidates = session.candidates();
        if (topCandidates.isEmpty()) {
            return buildResponse(
                    AIRecommendationStatus.UNAVAILABLE,
                    null,
                    List.of(),
                    "No hay asesores disponibles con información suficiente para recomendarte una opción ahora mismo.",
                    null,
                    null,
                    session.conversationId(),
                    session.questionsAsked(),
                    session.fallbackMode()
            );
        }

        RankedAdvisorCandidate bestCandidate = topCandidates.getFirst();
        NarrativeContent narrative = buildReadyNarrative(mergedContext, bestCandidate, topCandidates, shouldAttemptGemini);
        return buildResponse(
                AIRecommendationStatus.READY,
                bestCandidate.advisorId(),
                topCandidates,
                narrative.summary(),
                null,
                narrative.draftAppointmentMessage(),
                session.conversationId(),
                session.questionsAsked(),
                narrative.usedFallback()
        );
    }

    private RecommendationSession getActiveSession(String conversationId) {
        if (conversationId == null) {
            return null;
        }

        RecommendationSession session = recommendationSessions.get(conversationId);
        if (session == null) {
            return null;
        }

        if (!session.expiresAt().isAfter(Instant.now(clock))) {
            recommendationSessions.remove(conversationId);
            LOGGER.info("Recommendation session expired [conversationId={}]", conversationId);
            return null;
        }

        return session;
    }

    private RankingResult buildRankedCandidates(FarmerContext farmerContext, boolean shouldAttemptGemini) {
        LocalDate today = LocalDate.now(clock);
        List<RankedAdvisorCandidate> deterministicCandidates = advisorRepository.findRecommendationInputs(
                        AvailableDateStatus.AVAILABLE,
                        today
                ).stream()
                .map(projection -> toRankedCandidate(projection, farmerContext))
                .sorted(baseCandidateComparator())
                .toList();

        if (deterministicCandidates.isEmpty()) {
            return new RankingResult(List.of(), true);
        }

        SemanticScoringResult semanticScoringResult = scoreSemanticSimilarity(
                farmerContext.message(),
                deterministicCandidates.stream().limit(SEMANTIC_CANDIDATE_LIMIT).toList(),
                shouldAttemptGemini
        );

        List<RankedAdvisorCandidate> rankedCandidates = deterministicCandidates.stream()
                .map(candidate -> candidate.withSemanticScore(
                        semanticScoringResult.scores().getOrDefault(candidate.advisorId(), 0.0)
                ))
                .sorted(finalCandidateComparator())
                .toList();

        return new RankingResult(rankedCandidates, semanticScoringResult.usedFallback());
    }

    private RankedAdvisorCandidate toRankedCandidate(AdvisorRecommendationProjection projection, FarmerContext farmerContext) {
        double locationScore = computeLocationScore(farmerContext, projection);
        double ratingScore = computeRatingScore(projection.getRating());
        double experienceScore = computeExperienceScore(projection.getExperience());
        double availabilityScore = computeAvailabilityScore(projection.getNextAvailableDate());
        double lexicalScore = computeLexicalScore(
                farmerContext.message(),
                projection.getOccupation(),
                projection.getDescription(),
                projection.getSpokenLanguages()
        );

        return new RankedAdvisorCandidate(
                projection.getAdvisorId(),
                projection.getUserId(),
                projection.getRating(),
                projection.getFirstName(),
                projection.getLastName(),
                projection.getCity(),
                projection.getCountry(),
                projection.getDescription(),
                projection.getPhoto(),
                projection.getOccupation(),
                projection.getSpokenLanguages(),
                projection.getExperience(),
                projection.getNextAvailableDate(),
                locationScore + ratingScore + experienceScore + availabilityScore + lexicalScore,
                0.0,
                lexicalScore,
                locationScore,
                ratingScore,
                experienceScore,
                availabilityScore
        );
    }

    private Comparator<RankedAdvisorCandidate> baseCandidateComparator() {
        return Comparator.comparingDouble(RankedAdvisorCandidate::baseScore).reversed()
                .thenComparing(RankedAdvisorCandidate::rating, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(RankedAdvisorCandidate::experience, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(RankedAdvisorCandidate::nextAvailableDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(RankedAdvisorCandidate::advisorId);
    }

    private Comparator<RankedAdvisorCandidate> finalCandidateComparator() {
        return Comparator.comparingDouble(RankedAdvisorCandidate::finalScore).reversed()
                .thenComparing(RankedAdvisorCandidate::rating, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(RankedAdvisorCandidate::experience, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(RankedAdvisorCandidate::nextAvailableDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(RankedAdvisorCandidate::advisorId);
    }

    private AIRecommendationResponseDto buildResponse(
            AIRecommendationStatus status,
            Long selectedAdvisorId,
            List<RankedAdvisorCandidate> candidates,
            String summary,
            String clarifyingQuestion,
            String draftAppointmentMessage,
            String conversationId,
            int questionsAsked,
            boolean usedFallback
    ) {
        return new AIRecommendationResponseDto(
                status,
                selectedAdvisorId,
                candidates.stream().map(this::toMatchDto).toList(),
                summary,
                clarifyingQuestion,
                normalizeAppointmentMessage(draftAppointmentMessage),
                conversationId,
                questionsAsked,
                MAX_CLARIFYING_QUESTIONS,
                usedFallback
        );
    }

    private GeminiTextResult generateContent(String prompt, String flow) {
        try {
            String response = geminiGateway.generate(prompt);
            if (response == null || response.isBlank()) {
                logFallback(flow, "Gemini returned an empty response", prompt);
                return GeminiTextResult.failure(GeminiFailureType.UNAVAILABLE);
            }
            return GeminiTextResult.success(response);
        } catch (Exception exception) {
            GeminiFailureType failureType = classifyGeminiFailure(exception);
            LOGGER.warn(
                    "Gemini request failed [flow={}, classification={}, message={}]",
                    flow,
                    failureType,
                    exception.getMessage()
            );
            return GeminiTextResult.failure(failureType);
        }
    }

    private String requestGeminiContent(String prompt) throws Exception {
        if (httpClient == null || googleCredentials == null) {
            logFallback("vertex.request", "Vertex AI client is not initialized", prompt);
            return null;
        }

        String projectId = normalizeVertexConfig(vertexAiProjectId);
        String location = normalizeVertexConfig(vertexAiLocation);
        String modelId = normalizeVertexConfig(vertexAiModelId);
        if (projectId == null || location == null || modelId == null) {
            logFallback(
                    "vertex.request",
                    "Vertex AI configuration is incomplete",
                    "projectId=" + projectId + ", location=" + location + ", modelId=" + modelId
            );
            return null;
        }

        AccessToken accessToken = refreshAccessToken();
        String requestBody = buildVertexRequestBody(prompt);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildVertexEndpoint(projectId, location, modelId)))
                .header("Authorization", "Bearer " + accessToken.getTokenValue())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("Vertex AI error " + response.statusCode() + ": " + response.body());
        }

        return extractTextFromVertexResponse(response.body());
    }

    private GeminiFailureType classifyGeminiFailure(Exception exception) {
        String message = exception.getMessage();
        if (message == null) {
            return GeminiFailureType.OTHER;
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("429")
                || normalized.contains("quota")
                || normalized.contains("rate limit")
                || normalized.contains("too many requests")
                || normalized.contains("retry-after")
                || normalized.contains("resource exhausted")) {
            return GeminiFailureType.RATE_LIMIT;
        }

        return GeminiFailureType.OTHER;
    }

    private AccessToken refreshAccessToken() throws IOException {
        googleCredentials.refreshIfExpired();
        AccessToken accessToken = googleCredentials.getAccessToken();
        if (accessToken == null || accessToken.getTokenValue() == null || accessToken.getTokenValue().isBlank()) {
            throw new IOException("No access token available for Vertex AI");
        }
        return accessToken;
    }

    private String buildVertexRequestBody(String prompt) throws JsonProcessingException {
        Map<String, Object> payload = Map.of(
                "contents",
                List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", prompt))
                        )
                )
        );
        return objectMapper.writeValueAsString(payload);
    }

    private String buildVertexEndpoint(String projectId, String location, String modelId) {
        return "https://aiplatform.googleapis.com/v1/projects/"
                + projectId
                + "/locations/"
                + location
                + "/publishers/google/models/"
                + modelId
                + ":generateContent";
    }

    private String extractTextFromVertexResponse(String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray()) {
            logFallback("vertex.response", "Response does not contain candidates[]", responseBody);
            return null;
        }

        StringBuilder text = new StringBuilder();
        for (JsonNode candidate : candidates) {
            JsonNode parts = candidate.path("content").path("parts");
            if (!parts.isArray()) {
                continue;
            }
            for (JsonNode part : parts) {
                if (part.hasNonNull("text")) {
                    if (!text.isEmpty()) {
                        text.append('\n');
                    }
                    text.append(part.get("text").asText());
                }
            }
        }

        String aggregated = text.toString().trim();
        if (aggregated.isBlank()) {
            logFallback("vertex.response", "Response candidates did not include text parts", responseBody);
        }
        return aggregated.isBlank() ? null : aggregated;
    }

    private String normalizeVertexConfig(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private void logFallback(String flow, String reason, String details) {
        LOGGER.info(
                "AI fallback activated [flow={}, reason={}, details={}]",
                flow,
                reason,
                abbreviateForLog(details)
        );
    }

    private String abbreviateForLog(String value) {
        if (value == null) {
            return "n/a";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 220) {
            return normalized;
        }
        return normalized.substring(0, 217) + "...";
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
                    .append(", idiomas: ")
                    .append(advisor.spokenLanguages() == null || advisor.spokenLanguages().isBlank() ? "Español" : advisor.spokenLanguages())
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

    private boolean shouldAttemptGemini(String userMessage) {
        if (userMessage == null) {
            return false;
        }

        String normalized = userMessage.trim();
        if (normalized.isBlank()) {
            return false;
        }

        return normalized.matches(".*[\\p{L}\\p{N}].*");
    }

    private String sanitizeMessage(String message) {
        return message == null ? "" : message
                .replaceAll("(?im)^\\s*advisorId\\s*[:\\-]?.*$", "")
                .trim();
    }

    private FarmerContext resolveFarmerContext(String message) {
        String normalizedMessage = normalizeMessage(message);
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

        return new FarmerContext(normalizedMessage, city, country);
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return "";
        }
        return message.trim().replaceAll("\\s+", " ");
    }

    private double computeLocationScore(FarmerContext farmerContext, AdvisorRecommendationProjection advisorProfile) {
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
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(clock), nextAvailableDate);
        return Math.max(3.0, MAX_AVAILABILITY_SCORE - Math.min(daysUntil, 12));
    }

    private double computeLexicalScore(String userMessage, String occupation, String description, String spokenLanguages) {
        if (userMessage == null || userMessage.isBlank()) {
            return 0.0;
        }

        Set<String> messageTerms = tokenize(userMessage);
        if (messageTerms.isEmpty()) {
            return 0.0;
        }

        Set<String> advisorTerms = tokenize(String.join(
                " ",
                occupation == null ? "" : occupation,
                description == null ? "" : description,
                spokenLanguages == null ? "" : spokenLanguages
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
        return TOKEN_SPLITTER
                .splitAsStream(normalizeText(text) == null ? "" : normalizeText(text))
                .filter(token -> token.length() >= 4)
                .collect(Collectors.toSet());
    }

    private SemanticScoringResult scoreSemanticSimilarity(
            String userMessage,
            List<RankedAdvisorCandidate> candidates,
            boolean shouldAttemptGemini
    ) {
        if (!shouldAttemptGemini) {
            logFallback("recommendations.semantic", "Semantic scoring skipped because Gemini usage was disabled for this request", userMessage);
            return new SemanticScoringResult(Map.of(), true);
        }
        if (userMessage == null || userMessage.isBlank()) {
            logFallback("recommendations.semantic", "Semantic scoring skipped because the message is blank", userMessage);
            return new SemanticScoringResult(Map.of(), true);
        }
        if (candidates.isEmpty()) {
            logFallback("recommendations.semantic", "Semantic scoring skipped because there are no candidates", null);
            return new SemanticScoringResult(Map.of(), true);
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
            prompt.append("- advisorId: ").append(candidate.advisorId())
                    .append(", ocupacion: ").append(defaultText(candidate.occupation()))
                    .append(", descripcion: ").append(defaultText(candidate.description()))
                    .append(", idiomas: ").append(defaultText(candidate.spokenLanguages()))
                    .append('\n');
        }

        GeminiTextResult response = generateContent(prompt.toString(), "recommendations.semantic");
        JsonNode node = parseJsonResponse(response.text());
        if (node == null || !node.has("scores") || !node.get("scores").isArray()) {
            logFallback("recommendations.semantic", "Gemini semantic response could not be parsed into scores[]", response.text());
            return new SemanticScoringResult(Map.of(), true);
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

        return new SemanticScoringResult(scores, response.usedFallback());
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

    private AIRecommendationMatchDto toMatchDto(RankedAdvisorCandidate candidate) {
        return new AIRecommendationMatchDto(
                candidate.advisorId(),
                candidate.fullName(),
                candidate.occupation(),
                candidate.rating(),
                candidate.experience(),
                candidate.city(),
                candidate.country(),
                candidate.spokenLanguages(),
                candidate.nextAvailableDate(),
                buildWhy(candidate)
        );
    }

    private String buildWhy(RankedAdvisorCandidate candidate) {
        List<String> reasons = new ArrayList<>();
        if (candidate.locationScore() >= LOCATION_CITY_SCORE) {
            reasons.add("esta en tu misma ciudad");
        } else if (candidate.locationScore() >= LOCATION_COUNTRY_SCORE) {
            reasons.add("atiende en tu mismo país");
        }
        if (candidate.semanticScore() > 0 || candidate.lexicalScore() > 0) {
            reasons.add("su perfil se alinea con tu necesidad");
        }
        if (candidate.rating() != null) {
            reasons.add("tiene una calificación de " + candidate.rating().setScale(1, RoundingMode.HALF_UP));
        }
        if (candidate.experience() != null && candidate.experience() > 0) {
            reasons.add(candidate.experience() + " años de experiencia");
        }
        if (candidate.spokenLanguages() != null && !candidate.spokenLanguages().isBlank()) {
            reasons.add("puede atender en " + candidate.spokenLanguages());
        }
        if (candidate.nextAvailableDate() != null) {
            reasons.add("tiene disponibilidad desde el " + formatHumanDate(candidate.nextAvailableDate()));
        }
        if (reasons.isEmpty()) {
            reasons.add("puede orientarte segpun lo que describiste");
        }
        return "Destaca porque " + joinReasons(reasons) + ".";
    }

    private String joinReasons(List<String> reasons) {
        if (reasons.isEmpty()) {
            return "puede orientarte en este caso";
        }
        if (reasons.size() == 1) {
            return reasons.getFirst();
        }
        if (reasons.size() == 2) {
            return reasons.get(0) + " y " + reasons.get(1);
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < reasons.size(); index++) {
            if (index > 0) {
                builder.append(index == reasons.size() - 1 ? " y " : ", ");
            }
            builder.append(reasons.get(index));
        }
        return builder.toString();
    }

    private String formatHumanDate(LocalDate date) {
        return date.format(HUMAN_DATE_FORMATTER);
    }

    private NarrativeContent buildNeedsMoreInfoNarrative(
            FarmerContext farmerContext,
            List<RankedAdvisorCandidate> candidates,
            boolean shouldAttemptGemini
    ) {
        if (!shouldAttemptGemini) {
            logFallback("recommendations.needs_more_info", "Clarifying narrative used deterministic fallback because Gemini was skipped", farmerContext.message());
            return new NarrativeContent(
                    "Te muestro las opciones mas cercanas, pero necesito un poco mas de detalle para recomendarte una sola.",
                    "Que cultivo, problema específico o etapa del proceso agricola necesitas atender?",
                    null,
                    true
            );
        }

        String prompt = buildNarrativePrompt(
                farmerContext,
                candidates,
                AIRecommendationStatus.NEEDS_MORE_INFO,
                "Devuelve solo JSON valido con este formato: {\"summary\":\"texto\",\"clarifyingQuestion\":\"texto\",\"draftAppointmentMessage\":null}."
        );
        GeminiTextResult response = generateContent(prompt, "recommendations.needs_more_info");
        JsonNode node = parseJsonResponse(response.text());
        if (node != null && node.hasNonNull("summary") && node.hasNonNull("clarifyingQuestion")) {
            return new NarrativeContent(
                    node.get("summary").asText(),
                    node.get("clarifyingQuestion").asText(),
                    null,
                    response.usedFallback()
            );
        }

        logFallback("recommendations.needs_more_info", "Gemini clarifying response could not be parsed", response.text());
        return new NarrativeContent(
                "Te muestro las opciones mas cercanas, pero necesito un poco más de detalle para recomendarte una sola.",
                "Que cultivo, problema específico o etapa del proceso agrícola necesitas atender?",
                null,
                true
        );
    }

    private NarrativeContent buildReadyNarrative(
            FarmerContext farmerContext,
            RankedAdvisorCandidate bestCandidate,
            List<RankedAdvisorCandidate> candidates,
            boolean shouldAttemptGemini
    ) {
        if (!shouldAttemptGemini) {
            logFallback("recommendations.ready", "Ready narrative used deterministic fallback because Gemini was skipped", farmerContext.message());
            return buildForcedReadyNarrative(farmerContext, bestCandidate, candidates);
        }

        String prompt = buildNarrativePrompt(
                farmerContext,
                candidates,
                AIRecommendationStatus.READY,
                "Devuelve solo JSON valido con este formato: {\"summary\":\"texto\",\"clarifyingQuestion\":null,\"draftAppointmentMessage\":\"texto\"}."
        );
        GeminiTextResult response = generateContent(prompt, "recommendations.ready");
        JsonNode node = parseJsonResponse(response.text());
        if (node != null && node.hasNonNull("summary") && node.hasNonNull("draftAppointmentMessage")) {
            String summary = normalizeRecommendationSummary(node.get("summary").asText(), bestCandidate, candidates);
            String draftAppointmentMessage = normalizeAppointmentMessage(
                    node.get("draftAppointmentMessage").asText(),
                    bestCandidate,
                    candidates,
                    farmerContext
            );
            return new NarrativeContent(
                    summary,
                    null,
                    draftAppointmentMessage,
                    response.usedFallback()
            );
        }

        logFallback("recommendations.ready", "Gemini ready response could not be parsed", response.text());
        return buildForcedReadyNarrative(farmerContext, bestCandidate, candidates);
    }

    private NarrativeContent buildForcedReadyNarrative(
            FarmerContext farmerContext,
            RankedAdvisorCandidate bestCandidate,
            List<RankedAdvisorCandidate> candidates
    ) {
        return new NarrativeContent(
                buildSelectionSummary(bestCandidate),
                null,
                buildAppointmentContextMessage(bestCandidate.fullName(), defaultText(farmerContext.message())),
                true
        );
    }

    private String buildNarrativePrompt(
            FarmerContext farmerContext,
            List<RankedAdvisorCandidate> candidates,
            AIRecommendationStatus status,
            String outputInstruction
    ) {
        StringBuilder prompt = new StringBuilder("""
                Eres AgroBot. Redacta respuestas cortas, claras y útiles para un agricultor.
                No uses markdown.
                """);
        prompt.append(outputInstruction).append('\n')
                .append("Estado: ").append(status.name()).append('\n')
                .append("Consulta: ").append(defaultText(farmerContext.message())).append('\n')
                .append("Opciones:\n");

        if (status == AIRecommendationStatus.READY) {
            prompt.append("Asesor seleccionado: ")
                    .append(bestCandidateDescription(candidates.isEmpty() ? null : candidates.getFirst()))
                    .append('\n');
            prompt.append("""
                    Instrucciones para summary:
                    - Debe referirse exclusivamente al asesor seleccionado.
                    - No cambies de asesor ni menciones otro nombre.
                    - Resume por que se selecciono a este asesor.
                    - No digas que la cita ya fue programada.
                    - No anuncies una reunion confirmada.
                    - Si no hubo una coincidencia perfecta, puedes decirlo brevemente, pero explica por que esta fue la mejor opcion disponible.
                    Instrucciones para draftAppointmentMessage:
                    - Debe estar dirigido exclusivamente al asesor seleccionado.
                    - No cambies de asesor ni menciones otro nombre.
                    - El mensaje se envia al asesor despues de que la cita ya fue programada.
                    - No preguntes si la cita es posible.
                    - No pidas coordinar, reservar ni agendar una cita.
                    - Asume que la reunion sera virtual o en linea.
                    - El mensaje debe describir la situacion del productor agropecuario y lo que quiere revisar durante la reunion.
                    - Escribe el mensaje en primera persona, como si el productor agropecuario se lo escribiera directamente al asesor recomendado.
                    - No hables del productor agropecuario en tercera persona.
                    """);
        }

        if (farmerContext.city() != null || farmerContext.country() != null) {
            prompt.append("Ubicacion: ")
                    .append(defaultText(farmerContext.city()))
                    .append(", ")
                    .append(defaultText(farmerContext.country()))
                    .append('\n');
        }

        for (RankedAdvisorCandidate candidate : candidates) {
            prompt.append("- ")
                    .append(candidate.fullName())
                    .append(" | ocupacion: ").append(defaultText(candidate.occupation()))
                    .append(" | experiencia: ").append(candidate.experience() == null ? 0 : candidate.experience())
                    .append(" | idiomas: ").append(defaultText(candidate.spokenLanguages()))
                    .append(" | rating: ").append(candidate.rating() == null ? "sin dato" : candidate.rating())
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

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "No especificado" : value.trim();
    }

    private String normalizeAppointmentMessage(String draftAppointmentMessage) {
        if (draftAppointmentMessage == null || draftAppointmentMessage.isBlank()) {
            return draftAppointmentMessage;
        }

        String normalized = draftAppointmentMessage.trim().replaceAll("\\s+", " ");
        normalized = normalized.replaceAll("(?i)me gustar[ií]a coordinar una cita para revisar mi caso\\.?\\s*", "");
        normalized = normalized.replaceAll("(?i)quisiera coordinar una cita para revisar mi caso\\.?\\s*", "");
        normalized = normalized.replaceAll("(?i)me gustar[ií]a agendar una cita\\.?\\s*", "");
        normalized = normalized.replaceAll("(?i)quisiera agendar una cita\\.?\\s*", "");
        normalized = normalized.replaceAll("(?i)podr[ií]amos coordinar una cita\\??\\s*", "");
        normalized = normalized.replaceAll("(?i)es posible agendar una cita\\??\\s*", "");
        normalized = normalized.replaceAll("(?i)^estimado asesor,?\\s*", "");
        normalized = normalized.replaceAll("(?i)^el agricultor\\s+de\\s+[^.]+\\s+solicita una consulta sobre\\s*", "Quiero revisar ");
        normalized = normalized.replaceAll("(?i)^el agricultor\\s+solicita una consulta sobre\\s*", "Quiero revisar ");
        normalized = normalized.replaceAll("(?i)\\bdesea revisar\\b", "quiero revisar");
        normalized = normalized.replaceAll("(?i)^solicita una consulta sobre\\s*", "Quiero revisar ");

        if (!normalized.toLowerCase(Locale.ROOT).contains("reunion virtual")
                && !normalized.toLowerCase(Locale.ROOT).contains("reunion en linea")
                && !normalized.toLowerCase(Locale.ROOT).contains("reunión virtual")
                && !normalized.toLowerCase(Locale.ROOT).contains("reunión en línea")) {
            normalized = normalized + " Esto resume el contexto que quiero revisar en nuestra reunion virtual.";
        }

        return normalized.trim();
    }

    private String normalizeAppointmentMessage(
            String draftAppointmentMessage,
            RankedAdvisorCandidate bestCandidate,
            List<RankedAdvisorCandidate> candidates,
            FarmerContext farmerContext
    ) {
        if (draftAppointmentMessage == null || draftAppointmentMessage.isBlank()) {
            return buildAppointmentContextMessage(bestCandidate.fullName(), defaultText(farmerContext.message()));
        }

        if (mentionsDifferentCandidate(draftAppointmentMessage, bestCandidate, candidates)) {
            return buildAppointmentContextMessage(bestCandidate.fullName(), defaultText(farmerContext.message()));
        }

        String normalized = normalizeAppointmentMessage(draftAppointmentMessage);
        normalized = normalized.replaceFirst(
                "(?i)^(estimad[oa]\\s+[^,]+,\\s*|hola\\s+[^,]+,\\s*)",
                "Hola " + bestCandidate.fullName() + ", "
        );

        String lower = normalized.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("hola " + bestCandidate.fullName().toLowerCase(Locale.ROOT) + ",")) {
            normalized = "Hola " + bestCandidate.fullName() + ", " + normalized;
        }

        return normalized.trim();
    }

    private String normalizeRecommendationSummary(
            String summary,
            RankedAdvisorCandidate bestCandidate,
            List<RankedAdvisorCandidate> candidates
    ) {
        if (summary == null || summary.isBlank()) {
            return buildSelectionSummary(bestCandidate);
        }

        String normalized = summary.trim().replaceAll("\\s+", " ");
        if (mentionsDifferentCandidate(normalized, bestCandidate, candidates)) {
            return buildSelectionSummary(bestCandidate);
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.contains("hemos programado")
                || lower.contains("reunion virtual")
                || lower.contains("reunión virtual")
                || lower.contains("reunion en linea")
                || lower.contains("reunión en línea")
                || lower.contains("cita programada")
                || lower.contains("cita agendada")) {
            return buildSelectionSummary(bestCandidate);
        }
        return normalized;
    }

    private boolean mentionsDifferentCandidate(
            String text,
            RankedAdvisorCandidate bestCandidate,
            List<RankedAdvisorCandidate> candidates
    ) {
        if (text == null || text.isBlank() || candidates == null || candidates.isEmpty()) {
            return false;
        }

        String normalizedText = normalizeText(text);
        if (normalizedText == null) {
            return false;
        }

        for (RankedAdvisorCandidate candidate : candidates) {
            if (candidate == null || candidate.advisorId().equals(bestCandidate.advisorId())) {
                continue;
            }

            String normalizedName = normalizeText(candidate.fullName());
            if (normalizedName != null && normalizedText.contains(normalizedName)) {
                return true;
            }
        }

        return false;
    }

    private String bestCandidateDescription(RankedAdvisorCandidate bestCandidate) {
        if (bestCandidate == null) {
            return "No especificado";
        }

        return bestCandidate.fullName()
                + " (advisorId: " + bestCandidate.advisorId()
                + ", ocupacion: " + defaultText(bestCandidate.occupation())
                + ", ciudad: " + defaultText(bestCandidate.city())
                + ", país: " + defaultText(bestCandidate.country())
                + ", idiomas: " + defaultText(bestCandidate.spokenLanguages())
                + ")";
    }

    private String buildSelectionSummary(RankedAdvisorCandidate bestCandidate) {
        List<String> reasons = new ArrayList<>();
        if (bestCandidate.locationScore() >= LOCATION_CITY_SCORE) {
            reasons.add("coincide con tu ciudad");
        } else if (bestCandidate.locationScore() >= LOCATION_COUNTRY_SCORE) {
            reasons.add("atiende en tu mismo país");
        }
        if (bestCandidate.semanticScore() > 0 || bestCandidate.lexicalScore() > 0) {
            reasons.add("su perfil se alinea con tu necesidad");
        }
        if (bestCandidate.rating() != null) {
            reasons.add("tiene una calificación de " + bestCandidate.rating().setScale(1, RoundingMode.HALF_UP));
        }
        if (bestCandidate.experience() != null && bestCandidate.experience() > 0) {
            reasons.add("aporta " + bestCandidate.experience() + " años de experiencia");
        }
        if (bestCandidate.nextAvailableDate() != null) {
            reasons.add("cuenta con disponibilidad desde el " + formatHumanDate(bestCandidate.nextAvailableDate()));
        }

        if (reasons.isEmpty()) {
            return "Se selecciono a " + bestCandidate.fullName() + " porque fue la opcion mas consistente entre los asesores disponibles.";
        }
        if (bestCandidate.semanticScore() > 0 || bestCandidate.lexicalScore() > 0 || bestCandidate.locationScore() > 0) {
            return "Se selecciono a " + bestCandidate.fullName() + " porque " + joinReasons(reasons) + ".";
        }
        return "No hubo una coincidencia perfecta, pero se selecciono a " + bestCandidate.fullName() + " porque " + joinReasons(reasons) + ".";
    }

    private String buildAppointmentContextMessage(String advisorName, String situation) {
        return "Hola " + advisorName
                + ", quiero compartir el contexto de mi caso para nuestra reunion virtual. "
                + "Necesito asesoría sobre " + situation
                + " y quiero revisar contigo las posibles causas, el manejo recomendado y los siguientes pasos.";
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeConversationId(String conversationId) {
        if (conversationId == null) {
            return null;
        }
        String normalized = conversationId.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String mergeMessages(String originalMessage, String clarificationMessage) {
        LinkedHashSet<String> parts = new LinkedHashSet<>();
        if (originalMessage != null && !originalMessage.isBlank()) {
            parts.add(originalMessage.trim());
        }
        if (clarificationMessage != null && !clarificationMessage.isBlank()) {
            parts.add(clarificationMessage.trim());
        }
        return String.join(". ", parts);
    }

    private record FarmerContext(
            String message,
            String city,
            String country
    ) {
        private FarmerContext withMessage(String updatedMessage) {
            return new FarmerContext(updatedMessage, city, country);
        }
    }

    private record RankedAdvisorCandidate(
            Long advisorId,
            Long userId,
            BigDecimal rating,
            String firstName,
            String lastName,
            String city,
            String country,
            String description,
            String photo,
            String occupation,
            String spokenLanguages,
            Integer experience,
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
                    advisorId,
                    userId,
                    rating,
                    firstName,
                    lastName,
                    city,
                    country,
                    description,
                    photo,
                    occupation,
                    spokenLanguages,
                    experience,
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

        private String fullName() {
            String fullName = ((firstName == null ? "" : firstName.trim()) + " " + (lastName == null ? "" : lastName.trim())).trim();
            return fullName.isBlank() ? "Asesor disponible" : fullName;
        }
    }

    private record RecommendationSession(
            String conversationId,
            FarmerContext farmerContext,
            List<RankedAdvisorCandidate> candidates,
            int questionsAsked,
            boolean fallbackMode,
            Instant expiresAt
    ) {
    }

    private record RankingResult(
            List<RankedAdvisorCandidate> candidates,
            boolean usedFallback
    ) {
    }

    private record SemanticScoringResult(
            Map<Long, Double> scores,
            boolean usedFallback
    ) {
    }

    private record NarrativeContent(
            String summary,
            String clarifyingQuestion,
            String draftAppointmentMessage,
            boolean usedFallback
    ) {
    }

    private enum GeminiFailureType {
        RATE_LIMIT,
        UNAVAILABLE,
        OTHER
    }

    private record GeminiTextResult(
            String text,
            boolean usedFallback,
            GeminiFailureType failureType
    ) {
        private static GeminiTextResult success(String text) {
            return new GeminiTextResult(text, false, null);
        }

        private static GeminiTextResult failure(GeminiFailureType failureType) {
            return new GeminiTextResult(null, true, failureType);
        }
    }

    @FunctionalInterface
    interface GeminiGateway {
        String generate(String prompt) throws Exception;
    }
}
