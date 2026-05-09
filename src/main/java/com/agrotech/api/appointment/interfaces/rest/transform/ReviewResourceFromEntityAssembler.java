package com.agrotech.api.appointment.interfaces.rest.transform;

import com.agrotech.api.appointment.domain.model.entities.Review;
import com.agrotech.api.appointment.interfaces.rest.resources.ReviewResource;

public class ReviewResourceFromEntityAssembler {
    public static ReviewResource toResourceFromEntity(Review entity){
        return new ReviewResource(
                entity.getId(),
                entity.getAdvisorId(),
                entity.getFarmerId(),
                entity.getComment(),
                entity.getRating()
        );
    }
}
