package com.agrotech.api.appointment.application.mapper;

import com.agrotech.api.appointment.infrastructure.web.dto.AppointmentResource;
import com.agrotech.api.appointment.infrastructure.web.dto.AvailableDateResource;
import com.agrotech.api.appointment.infrastructure.web.dto.ReviewResource;
import com.agrotech.api.appointment.domain.model.Appointment;
import com.agrotech.api.appointment.domain.model.AvailableDate;
import com.agrotech.api.appointment.domain.model.Review;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    public AppointmentResource toAppointmentResource(Appointment appointment) {
        return new AppointmentResource(
                appointment.getId(),
                appointment.getFarmer().getId(),
                toAvailableDateResource(appointment.getAvailableDate()),
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
                review.getComment(),
                review.getRating()
        );
    }
}
