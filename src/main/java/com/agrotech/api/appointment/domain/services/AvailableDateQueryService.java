package com.agrotech.api.appointment.domain.services;

import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface AvailableDateQueryService {
    List<AvailableDate> handle(GetAllAvailableDatesQuery query);
    Optional<AvailableDate> handle(GetAvailableDateByIdQuery query);
    List<AvailableDate> handle(GetAvailableDatesByAdvisorIdQuery query);
    List<AvailableDate> handle(GetAvailableDateByAdvisorIdAndStatusQuery query);
    List<AvailableDate> handle(GetAvailableDateByStatusQuery query);
    Optional<AvailableDate> handle(GetAvailableDateByAdvisorIdAndDate query);
}
