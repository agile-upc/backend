package com.agrotech.api.appointment.infrastructure.web.dto;

import com.agrotech.api.profile.infrastructure.web.dto.FarmerProfileSummaryResource;

public record ReviewResource(Long id,
                             Long advisorId,
                             Long farmerId,
                             FarmerProfileSummaryResource farmerProfile,
                             String comment,
                             Integer rating) {
}
