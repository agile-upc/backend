package com.agrotech.api.appointment.application.usecase;

import com.agrotech.api.appointment.domain.valueobject.AppointmentStatus;
import com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus;
import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.appointment.domain.model.Appointment;
import com.agrotech.api.appointment.domain.model.AvailableDate;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repository.AppointmentRepository;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repository.AvailableDateRepository;
import com.agrotech.api.appointment.infrastructure.web.dto.CreateAppointmentResource;
import com.agrotech.api.appointment.infrastructure.web.dto.UpdateAppointmentResource;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Farmer;
import com.agrotech.api.profile.domain.model.Profile;
import com.agrotech.api.profile.application.usecase.NotificationService;
import com.agrotech.api.profile.application.usecase.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AvailableDateRepository availableDateRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;
    private final AuthenticatedUserService authenticatedUserService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AvailableDateRepository availableDateRepository,
            ProfileService profileService,
            NotificationService notificationService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.availableDateRepository = availableDateRepository;
        this.profileService = profileService;
        this.notificationService = notificationService;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<Appointment> getAppointments() {
        AuthenticatedUser authenticatedUser = authenticatedUserService.getCurrentUser();
        List<Appointment> appointments;

        if (authenticatedUser.role() == UserRole.FARMER) {
            appointments = appointmentRepository.findByFarmer_Id(authenticatedUser.farmerId());
        } else if (authenticatedUser.role() == UserRole.ADVISOR) {
            appointments = appointmentRepository.findByAvailableDate_Advisor_Id(authenticatedUser.advisorId());
        } else {
            appointments = appointmentRepository.findAll();
        }

        return appointments.stream()
                .map(this::refreshStatusIfNeeded)
                .toList();
    }

    public Appointment getAppointmentById(Long id) {
        return refreshStatusIfNeeded(requireAppointment(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public Appointment createAppointment(CreateAppointmentResource resource) {
        Farmer farmer = profileService.requireCurrentFarmerEntity();
        AvailableDate availableDate = requireAvailableDate(resource.availableDateId());

        if (availableDate.getStatus() == AvailableDateStatus.UNAVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected available date is already booked");
        }

        Appointment appointment = appointmentRepository.save(Appointment.builder()
                .farmer(farmer)
                .availableDate(availableDate)
                .message(resource.message())
                .status(AppointmentStatus.PENDING)
                .meetingUrl("https://meet.jit.si/agrotechMeeting" + farmer.getId() + "-" + availableDate.getAdvisor().getId())
                .build());

        availableDate.setStatus(AvailableDateStatus.UNAVAILABLE);
        availableDateRepository.save(availableDate);

        Advisor advisor = availableDate.getAdvisor();
        Profile farmerProfile = profileService.getProfileEntityByFarmerId(farmer.getId());
        Profile advisorProfile = profileService.getProfileEntityByAdvisorId(advisor.getId());

        notificationService.createNotification(
                farmer.getUser().getId(),
                "Proximo Asesoramiento",
                "Tienes un asesoramiento programado con " + fullName(advisorProfile),
                new Date()
        );
        notificationService.createNotification(
                advisor.getUser().getId(),
                "Proximo Asesoramiento",
                "Tienes una asesoria programada con " + fullName(farmerProfile),
                new Date()
        );

        return appointment;
    }

    @Transactional(rollbackFor = Exception.class)
    public Appointment updateAppointment(Long id, UpdateAppointmentResource resource) {
        Appointment appointment = requireAppointment(id);
        appointment.setMessage(resource.message());
        if (resource.status() != null) {
            try {
                appointment.setStatus(AppointmentStatus.valueOf(resource.status().toUpperCase()));
            } catch (IllegalArgumentException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid appointment status");
            }
        }
        return appointmentRepository.save(appointment);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAppointment(Long id) {
        Appointment appointment = requireAppointment(id);
        AvailableDate availableDate = appointment.getAvailableDate();
        availableDate.setStatus(AvailableDateStatus.AVAILABLE);
        availableDateRepository.save(availableDate);

        notificationService.createNotification(
                availableDate.getAdvisor().getUser().getId(),
                "Cita Cancelada",
                "Se ha cancelado una cita programada para el dia " + availableDate.getScheduledDate() + " a las " + availableDate.getStartTime(),
                new Date()
        );

        appointmentRepository.delete(appointment);
    }

    private Appointment requireAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private AvailableDate requireAvailableDate(Long id) {
        return availableDateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Available date not found"));
    }

    private Appointment refreshStatusIfNeeded(Appointment appointment) {
        AvailableDate availableDate = appointment.getAvailableDate();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(availableDate.getScheduledDate(), LocalTime.parse(availableDate.getStartTime()));
        LocalDateTime end = LocalDateTime.of(availableDate.getScheduledDate(), LocalTime.parse(availableDate.getEndTime()));

        AppointmentStatus nextStatus = appointment.getStatus();
        if (now.isAfter(end)) {
            nextStatus = AppointmentStatus.COMPLETED;
        } else if (now.isAfter(start)) {
            nextStatus = AppointmentStatus.ONGOING;
        }

        if (nextStatus != appointment.getStatus()) {
            appointment.setStatus(nextStatus);
            return appointmentRepository.save(appointment);
        }
        return appointment;
    }

    private String fullName(Profile profile) {
        String firstName = profile.getFirstName() == null ? "" : profile.getFirstName().trim();
        String lastName = profile.getLastName() == null ? "" : profile.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? "tu asesor" : fullName;
    }
}
