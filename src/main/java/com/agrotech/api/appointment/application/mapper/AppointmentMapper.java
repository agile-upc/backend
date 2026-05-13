package com.agrotech.api.appointment.application.mapper;

import com.agrotech.api.appointment.infrastructure.web.dto.AppointmentResource;
import com.agrotech.api.appointment.infrastructure.web.dto.AvailableDateResource;
import com.agrotech.api.appointment.infrastructure.web.dto.ReviewResource;
import com.agrotech.api.appointment.domain.model.Appointment;
import com.agrotech.api.appointment.domain.model.AvailableDate;
import com.agrotech.api.appointment.domain.model.Review;
import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.application.mapper.ProfileMapper;
import com.agrotech.api.profile.application.usecase.ProfileService;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorProfileSummaryResource;
import com.agrotech.api.profile.infrastructure.web.dto.FarmerProfileSummaryResource;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    public AppointmentMapper(
            AuthenticatedUserService authenticatedUserService,
            ProfileService profileService,
            ProfileMapper profileMapper
    ) {
        this.authenticatedUserService = authenticatedUserService;
        this.profileService = profileService;
        this.profileMapper = profileMapper;
    }

    public AppointmentResource toAppointmentResource(Appointment appointment) {
        AuthenticatedUser authenticatedUser = authenticatedUserService.getCurrentUser();
        AdvisorProfileSummaryResource advisorProfileSummary = shouldIncludeAdvisorProfile(authenticatedUser.role())
                ? profileMapper.toAdvisorProfileSummaryResource(
                        profileService.getProfileEntityByAdvisorId(appointment.getAvailableDate().getAdvisor().getId())
                )
                : null;
        FarmerProfileSummaryResource farmerProfileSummary = shouldIncludeFarmerProfile(authenticatedUser.role())
                ? profileMapper.toFarmerProfileSummaryResource(
                        profileService.getProfileEntityByFarmerId(appointment.getFarmer().getId())
                )
                : null;

        return new AppointmentResource(
                appointment.getId(),
                appointment.getFarmer().getId(),
                toAvailableDateResource(appointment.getAvailableDate()),
                advisorProfileSummary,
                farmerProfileSummary,
                appointment.getMessage(),
                appointment.getStatus().name(),
                appointment.getMeetingUrl()
        );
    }

    public AvailableDateResource toAvailableDateResource(AvailableDate availableDate) {
        return new AvailableDateResource(
                availableDate.getId(),
                availableDate.getAdvisor().getId(),
                availableDate.getScheduledDate(),
                availableDate.getStartTime(),
                availableDate.getEndTime(),
                availableDate.getStatus().name()
        );
    }

    public ReviewResource toReviewResource(Review review) {
        return new ReviewResource(
                review.getId(),
                review.getAdvisor().getId(),
                review.getFarmer().getId(),
                profileMapper.toFarmerProfileSummaryResource(
                        profileService.getProfileEntityByFarmerId(review.getFarmer().getId())
                ),
                review.getComment(),
                review.getRating()
        );
    }

    private boolean shouldIncludeAdvisorProfile(UserRole role) {
        return role == UserRole.FARMER || role == UserRole.ADMIN;
    }

    private boolean shouldIncludeFarmerProfile(UserRole role) {
        return role == UserRole.ADVISOR || role == UserRole.ADMIN;
    }
}
