package com.agrotech.api.profile.application.usecase;

import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.application.mapper.ProfileMapper;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Profile;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.AdvisorRepository;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.ProfileRepository;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorCatalogResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdvisorService {
    private final AdvisorRepository advisorRepository;
    private final ProfileRepository profileRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileMapper profileMapper;

    public AdvisorService(
            AdvisorRepository advisorRepository,
            ProfileRepository profileRepository,
            AuthenticatedUserService authenticatedUserService,
            ProfileMapper profileMapper
    ) {
        this.advisorRepository = advisorRepository;
        this.profileRepository = profileRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.profileMapper = profileMapper;
    }

    public List<AdvisorCatalogResource> getAdvisorCatalog() {
        return advisorRepository.findAll().stream()
                .map(advisor -> profileMapper.toAdvisorCatalogResource(
                        advisor,
                        getProfileByUserId(advisor.getUser().getId())
                ))
                .toList();
    }

    public AdvisorCatalogResource getAdvisorCatalogById(Long id) {
        Advisor advisor = getAdvisorEntity(id);
        return profileMapper.toAdvisorCatalogResource(
                advisor,
                getProfileByUserId(advisor.getUser().getId())
        );
    }

    public Advisor getAdvisorById(Long id) {
        return getAdvisorEntity(id);
    }

    public Advisor getAdvisorByUserId(Long userId) {
        return getAdvisorEntityByUserId(userId);
    }

    public Advisor getCurrentAdvisor() {
        Long advisorId = authenticatedUserService.requireRole(UserRole.ADVISOR).advisorId();
        return getAdvisorEntity(advisorId);
    }

    public void deleteAdvisor(Long id) {
        advisorRepository.delete(getAdvisorEntity(id));
    }

    public List<AdvisorRecommendationOption> getAdvisorRecommendationOptions() {
        Map<Long, Profile> profilesByUserId = profileRepository.findByUser_Role(UserRole.ADVISOR).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));

        return advisorRepository.findAll().stream()
                .map(advisor -> {
                    Profile profile = profilesByUserId.get(advisor.getUser().getId());
                    if (profile == null) {
                        return null;
                    }

                    String fullName = (profile.getFirstName() + " " + profile.getLastName()).trim();
                    return new AdvisorRecommendationOption(
                            advisor.getId(),
                            fullName,
                            profile.getOccupation()
                    );
                })
                .filter(option -> option != null)
                .toList();
    }

    public record AdvisorRecommendationOption(
            Long advisorId,
            String fullName,
            String occupation
    ) {
    }

    private Advisor getAdvisorEntity(Long id) {
        return advisorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advisor not found"));
    }

    private Advisor getAdvisorEntityByUserId(Long userId) {
        return advisorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advisor not found"));
    }

    private Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }
}
