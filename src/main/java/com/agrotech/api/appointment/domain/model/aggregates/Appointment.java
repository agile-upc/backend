package com.agrotech.api.appointment.domain.model.aggregates;

import com.agrotech.api.appointment.domain.exceptions.InvalidAvailableDateException;
import com.agrotech.api.appointment.domain.exceptions.InvalidStatusException;
import com.agrotech.api.appointment.domain.model.commands.CreateAppointmentCommand;
import com.agrotech.api.appointment.domain.model.commands.UpdateAppointmentCommand;
import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.domain.model.valueobjects.AppointmentStatus;
import com.agrotech.api.appointment.domain.model.valueobjects.AvailableDateStatus;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class Appointment {
    private Long id;
    private String message;
    private AppointmentStatus status;
    private String meetingUrl;
    private Farmer farmer;
    private AvailableDate availableDate;

    public Appointment() {}

    private Appointment(String message, String meetingUrl, Farmer farmer, AvailableDate availableDate) {
        this.message = message;
        this.status = AppointmentStatus.PENDING;
        this.meetingUrl = meetingUrl;
        this.farmer = farmer;
        this.availableDate = availableDate;
    }

    public Appointment(Long id, String message, AppointmentStatus status, String meetingUrl, Farmer farmer, AvailableDate availableDate) {
        this.id = id;
        this.message = message;
        this.status = status;
        this.meetingUrl = meetingUrl;
        this.farmer = farmer;
        this.availableDate = availableDate;
    }

    public static Appointment create(CreateAppointmentCommand command, Farmer farmer, AvailableDate availableDate) {
        if (availableDate.getStatus() == AvailableDateStatus.UNAVAILABLE) {
            throw new InvalidAvailableDateException(availableDate.getId());
        }

        String meetingUrl = "https://meet.jit.si/agrotechMeeting" + command.farmerId() + "-" + availableDate.getAdvisorId();

        return new Appointment(command.message(), meetingUrl, farmer, availableDate);
    }

    public void updateStatusBasedOnTime(LocalDateTime now) {
        LocalDateTime start = LocalDateTime.of(availableDate.getScheduledDate(), LocalTime.parse(availableDate.getStartTime()));
        LocalDateTime end = LocalDateTime.of(availableDate.getScheduledDate(), LocalTime.parse(availableDate.getEndTime()));

        if (now.isAfter(end)) {
            this.status = AppointmentStatus.COMPLETED;
        } else if (now.isAfter(start)) {
            this.status = AppointmentStatus.ONGOING;
        }
    }

    public static void validateStatus(String status) {
        if (!status.matches("^(?i)(PENDING|ONGOING|COMPLETED)$")) {
            throw new InvalidStatusException(status);
        }
    }

    public Long getAvailableDateId() {
        return availableDate.getId();
    }

    public Long getFarmerId() {
        return farmer.getId();
    }

    public String getAppointmentStatus() {
        return status.name();
    }
}