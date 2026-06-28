package com.agrotech.api.profile.application.mapper;

import com.agrotech.api.iam.domain.model.User;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorResource;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorCatalogResource;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorProfileSummaryResource;
import com.agrotech.api.profile.infrastructure.web.dto.CreateProfileResource;
import com.agrotech.api.profile.infrastructure.web.dto.FarmerResource;
import com.agrotech.api.profile.infrastructure.web.dto.FarmerProfileSummaryResource;
import com.agrotech.api.profile.infrastructure.web.dto.ProfileResource;
import com.agrotech.api.profile.infrastructure.web.dto.UpdateProfileResource;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Farmer;
import com.agrotech.api.profile.domain.model.Profile;
import com.agrotech.api.profile.infrastructure.persistence.jpa.projection.AdvisorRecommendationProjection;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {
    private static final String DEFAULT_ADVISOR_LANGUAGE = "Español";

    public ProfileResource toProfileResource(Profile profile) {
        return new ProfileResource(
                profile.getId(),
                profile.getUser().getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getCity(),
                profile.getCountry(),
                profile.getBirthDate(),
                profile.getDescription(),
                profile.getPhoto(),
                profile.getOccupation(),
                spokenLanguagesFor(profile),
                profile.getExperience()
        );
    }

    public FarmerResource toFarmerResource(Farmer farmer) {
        return new FarmerResource(farmer.getId(), farmer.getUser().getId());
    }

    public AdvisorResource toAdvisorResource(Advisor advisor) {
        return toAdvisorResource(advisor, false);
    }

    public AdvisorResource toAdvisorResource(Advisor advisor, boolean validated) {
        return new AdvisorResource(advisor.getId(), advisor.getUser().getId(), advisor.getRating(), validated);
    }

    public AdvisorCatalogResource toAdvisorCatalogResource(Advisor advisor, Profile profile) {
        return toAdvisorCatalogResource(advisor, profile, false);
    }

    public AdvisorCatalogResource toAdvisorCatalogResource(Advisor advisor, Profile profile, boolean validated) {
        return new AdvisorCatalogResource(
                advisor.getId(),
                advisor.getUser().getId(),
                advisor.getRating(),
                toAdvisorProfileSummaryResource(profile),
                validated
        );
    }

    public AdvisorCatalogResource toAdvisorCatalogResource(AdvisorRecommendationProjection projection) {
        return toAdvisorCatalogResource(projection, false);
    }

    public AdvisorCatalogResource toAdvisorCatalogResource(AdvisorRecommendationProjection projection, boolean validated) {
        return new AdvisorCatalogResource(
                projection.getAdvisorId(),
                projection.getUserId(),
                projection.getRating(),
                new AdvisorProfileSummaryResource(
                        projection.getProfileId(),
                        projection.getUserId(),
                        projection.getFirstName(),
                        projection.getLastName(),
                        projection.getCity(),
                        projection.getCountry(),
                        projection.getBirthDate(),
                        projection.getDescription(),
                        projection.getPhoto(),
                        projection.getOccupation(),
                        defaultSpokenLanguages(projection.getSpokenLanguages()),
                        projection.getExperience()
                ),
                validated
        );
    }

    public AdvisorProfileSummaryResource toAdvisorProfileSummaryResource(Profile profile) {
        return new AdvisorProfileSummaryResource(
                profile.getId(),
                profile.getUser().getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getCity(),
                profile.getCountry(),
                profile.getBirthDate(),
                profile.getDescription(),
                profile.getPhoto(),
                profile.getOccupation(),
                spokenLanguagesFor(profile),
                profile.getExperience()
        );
    }

    public FarmerProfileSummaryResource toFarmerProfileSummaryResource(Profile profile) {
        return new FarmerProfileSummaryResource(
                profile.getId(),
                profile.getUser().getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getCity(),
                profile.getCountry(),
                profile.getBirthDate(),
                profile.getDescription(),
                profile.getPhoto(),
                profile.getOccupation(),
                profile.getSpokenLanguages(),
                profile.getExperience()
        );
    }

    public Profile toProfile(CreateProfileResource resource, User user, String photo) {
        return Profile.builder()
                .user(user)
                .firstName(resource.firstName())
                .lastName(resource.lastName())
                .city(resource.city())
                .country(resource.country())
                .birthDate(resource.birthDate())
                .description(resource.description())
                .occupation(resource.occupation())
                .spokenLanguages(spokenLanguagesFor(user, resource.spokenLanguages()))
                .experience(resource.experience() != null ? resource.experience() : 0)
                .photo(photo)
                .build();
    }

    public void updateProfile(Profile profile, UpdateProfileResource resource, String uploadedPhoto) {
        profile.setFirstName(resource.firstName());
        profile.setLastName(resource.lastName());
        profile.setCity(resource.city());
        profile.setCountry(resource.country());
        profile.setBirthDate(resource.birthDate());
        profile.setDescription(resource.description());
        profile.setOccupation(resource.occupation());
        profile.setSpokenLanguages(spokenLanguagesFor(profile.getUser(), resource.spokenLanguages()));
        profile.setExperience(resource.experience() != null ? resource.experience() : 0);
        if (uploadedPhoto != null) {
            profile.setPhoto(uploadedPhoto);
        }
    }

    private String spokenLanguagesFor(Profile profile) {
        return spokenLanguagesFor(profile.getUser(), profile.getSpokenLanguages());
    }

    private String spokenLanguagesFor(User user, String value) {
        if (user != null && user.getRole() == UserRole.ADVISOR) {
            return defaultSpokenLanguages(value);
        }

        return value;
    }

    private String defaultSpokenLanguages(String value) {
        return value == null || value.isBlank() ? DEFAULT_ADVISOR_LANGUAGE : value;
    }
}
