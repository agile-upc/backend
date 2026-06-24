package com.agrotech.api.profile.application.mapper;

import com.agrotech.api.iam.domain.model.User;
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
                profile.getSpokenLanguages(),
                profile.getExperience()
        );
    }

    public FarmerResource toFarmerResource(Farmer farmer) {
        return new FarmerResource(farmer.getId(), farmer.getUser().getId());
    }

    public AdvisorResource toAdvisorResource(Advisor advisor) {
        return new AdvisorResource(advisor.getId(), advisor.getUser().getId(), advisor.getRating());
    }

    public AdvisorCatalogResource toAdvisorCatalogResource(Advisor advisor, Profile profile) {
        return new AdvisorCatalogResource(
                advisor.getId(),
                advisor.getUser().getId(),
                advisor.getRating(),
                toAdvisorProfileSummaryResource(profile)
        );
    }

    public AdvisorCatalogResource toAdvisorCatalogResource(AdvisorRecommendationProjection projection) {
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
                        projection.getSpokenLanguages(),
                        projection.getExperience()
                )
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
                profile.getSpokenLanguages(),
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
                .spokenLanguages(resource.spokenLanguages())
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
        profile.setSpokenLanguages(resource.spokenLanguages());
        profile.setExperience(resource.experience() != null ? resource.experience() : 0);
        if (uploadedPhoto != null) {
            profile.setPhoto(uploadedPhoto);
        }
    }
}
