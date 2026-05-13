package com.agrotech.api.appointment.infrastructure.web.dto;

import com.agrotech.api.profile.infrastructure.web.dto.AdvisorProfileSummaryResource;
import com.agrotech.api.profile.infrastructure.web.dto.FarmerProfileSummaryResource;

public record AppointmentResource(Long id,
                                  Long farmerId,
                                  AvailableDateResource availableDate,
                                  AdvisorProfileSummaryResource advisorProfileSummary,
                                  FarmerProfileSummaryResource farmerProfileSummary,
                                  String message,
                                  String status,
                                  String meetingUrl) {
}
