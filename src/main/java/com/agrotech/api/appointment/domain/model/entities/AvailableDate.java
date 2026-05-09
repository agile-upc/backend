package com.agrotech.api.appointment.domain.model.entities;

import com.agrotech.api.appointment.domain.exceptions.IncorrectTimeFormatException;
import com.agrotech.api.appointment.domain.exceptions.InvalidDateException;
import com.agrotech.api.appointment.domain.exceptions.InvalidTimeRangeException;
import com.agrotech.api.appointment.domain.model.commands.CreateAvailableDateCommand;
import com.agrotech.api.appointment.domain.model.valueobjects.AvailableDateStatus;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class AvailableDate {
    private Long id;
    private Advisor advisor;
    private LocalDate scheduledDate;
    private String startTime;
    private String endTime;
    private AvailableDateStatus status;

    public AvailableDate() {
    }

    public AvailableDate(Long id, LocalDate scheduledDate, String startTime, String endTime, AvailableDateStatus status, Advisor advisor) {
        this.id = id;
        this.scheduledDate = scheduledDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.advisor = advisor;
    }

    private AvailableDate(LocalDate scheduledDate, String startTime, String endTime, Advisor advisor) {
        this.scheduledDate = scheduledDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.advisor = advisor;
        this.status = AvailableDateStatus.AVAILABLE;
    }

    public static AvailableDate create(CreateAvailableDateCommand command, Advisor advisor) {
        validateScheduledDate(command.scheduledDate());
        validateTimeFormat(command.startTime(), command.endTime());
        validateTimeRange(command.startTime(), command.endTime());

        return new AvailableDate(command.scheduledDate(), command.startTime(), command.endTime(), advisor);
    }

    public Long getAdvisorId() {
        return advisor.getId();
    }

    public String getAvailableDateStatus() {
        return status.name();
    }

    public static void validateScheduledDate(LocalDate scheduledDate) {
        if (scheduledDate.isBefore(LocalDateTime.now().toLocalDate())) {
            throw new InvalidDateException(scheduledDate);
        }
    }

    public static void validateTimeFormat(String startTime, String endTime) {
        if (!startTime.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
                || !endTime.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            throw new IncorrectTimeFormatException(startTime, endTime);
        }
    }

    public static void validateTimeRange(String startTime, String endTime) {
        if (startTime.compareTo(endTime) >= 0) {
            throw new InvalidTimeRangeException(startTime, endTime);
        }
    }
}