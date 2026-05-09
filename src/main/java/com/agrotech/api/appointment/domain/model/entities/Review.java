package com.agrotech.api.appointment.domain.model.entities;

import com.agrotech.api.appointment.domain.exceptions.InvalidRatingException;
import com.agrotech.api.appointment.domain.model.commands.CreateReviewCommand;
import com.agrotech.api.appointment.domain.model.commands.UpdateReviewCommand;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import lombok.Getter;

@Getter
public class Review {
    private Long id;
    private Advisor advisor;
    private Farmer farmer;
    private String comment;
    private Integer rating;

    public Review() {}

    private Review(String comment, Integer rating, Advisor advisor, Farmer farmer) {
        this.comment = comment;
        this.rating = rating;
        this.advisor = advisor;
        this.farmer = farmer;
    }

    public Review(Long id, String comment, Integer rating, Advisor advisor, Farmer farmer) {
        this.id = id;
        this.comment = comment;
        this.rating = rating;
        this.advisor = advisor;
        this.farmer = farmer;
    }

    public static Review create(CreateReviewCommand command, Advisor advisor, Farmer farmer) {
        validateRating(command.rating());
        return new Review(command.comment(), command.rating(), advisor, farmer);
    }

    public static void validateRating(Integer rating) {
        if (rating < 0 || rating > 5) {
            throw new InvalidRatingException(rating);
        }
    }

    public Long getAdvisorId() {
        return advisor.getId();
    }

    public Long getFarmerId() {
        return farmer.getId();
    }
}