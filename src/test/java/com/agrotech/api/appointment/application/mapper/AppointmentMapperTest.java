package com.agrotech.api.appointment.application.mapper;

import com.agrotech.api.appointment.domain.model.Appointment;
import com.agrotech.api.appointment.domain.model.AvailableDate;
import com.agrotech.api.appointment.domain.valueobject.AppointmentStatus;
import com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus;
import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.model.User;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.application.mapper.ProfileMapper;
import com.agrotech.api.profile.application.usecase.ProfileService;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Farmer;
import com.agrotech.api.profile.domain.model.Profile;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentMapperTest {
    private final AuthenticatedUserService authenticatedUserService = mock(AuthenticatedUserService.class);
    private final ProfileService profileService = mock(ProfileService.class);
    private final ProfileMapper profileMapper = new ProfileMapper();

    private final AppointmentMapper appointmentMapper = new AppointmentMapper(
            authenticatedUserService,
            profileService,
            profileMapper
    );

    @Test
    void toAppointmentResourceForFarmerIncludesAdvisorProfileSummary() {
        Appointment appointment = buildAppointment();
        Profile advisorProfile = buildProfile(30L, 200L, UserRole.ADVISOR, "Ana", "Lopez");

        when(authenticatedUserService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(100L, 10L, "farmer@test.com", UserRole.FARMER, 1L, null)
        );
        when(profileService.getProfileEntityByAdvisorId(2L)).thenReturn(advisorProfile);

        var resource = appointmentMapper.toAppointmentResource(appointment);

        assertNotNull(resource.advisorProfileSummary());
        assertEquals("Ana", resource.advisorProfileSummary().firstName());
        assertNull(resource.farmerProfileSummary());
        verify(profileService).getProfileEntityByAdvisorId(2L);
        verify(profileService, never()).getProfileEntityByFarmerId(1L);
    }

    @Test
    void toAppointmentResourceForAdvisorIncludesFarmerProfileSummary() {
        Appointment appointment = buildAppointment();
        Profile farmerProfile = buildProfile(40L, 100L, UserRole.FARMER, "Luis", "Quispe");

        when(authenticatedUserService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(200L, 20L, "advisor@test.com", UserRole.ADVISOR, null, 2L)
        );
        when(profileService.getProfileEntityByFarmerId(1L)).thenReturn(farmerProfile);

        var resource = appointmentMapper.toAppointmentResource(appointment);

        assertNull(resource.advisorProfileSummary());
        assertNotNull(resource.farmerProfileSummary());
        assertEquals("Luis", resource.farmerProfileSummary().firstName());
        verify(profileService).getProfileEntityByFarmerId(1L);
        verify(profileService, never()).getProfileEntityByAdvisorId(2L);
    }

    private Appointment buildAppointment() {
        Farmer farmer = Farmer.builder()
                .id(1L)
                .user(User.builder().id(100L).role(UserRole.FARMER).build())
                .build();
        Advisor advisor = Advisor.builder()
                .id(2L)
                .user(User.builder().id(200L).role(UserRole.ADVISOR).build())
                .build();
        AvailableDate availableDate = AvailableDate.builder()
                .id(3L)
                .advisor(advisor)
                .scheduledDate(LocalDate.of(2026, 5, 20))
                .startTime("09:00")
                .endTime("10:00")
                .status(AvailableDateStatus.UNAVAILABLE)
                .build();

        return Appointment.builder()
                .id(4L)
                .farmer(farmer)
                .availableDate(availableDate)
                .message("Necesito apoyo")
                .status(AppointmentStatus.PENDING)
                .meetingUrl("https://meet.example.com/4")
                .build();
    }

    private Profile buildProfile(Long profileId, Long userId, UserRole role, String firstName, String lastName) {
        return Profile.builder()
                .id(profileId)
                .user(User.builder().id(userId).role(role).build())
                .firstName(firstName)
                .lastName(lastName)
                .city("Cusco")
                .country("Peru")
                .occupation("Agronomo")
                .experience(5)
                .build();
    }
}
