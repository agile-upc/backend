package com.agrotech.api.appointment.application.internal.commandservices;

import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalProfilesService;
import com.agrotech.api.appointment.domain.exceptions.*;
import com.agrotech.api.appointment.domain.model.commands.CreateReviewCommand;
import com.agrotech.api.appointment.domain.model.commands.DeleteReviewCommand;
import com.agrotech.api.appointment.domain.model.commands.UpdateReviewCommand;
import com.agrotech.api.appointment.domain.model.entities.Review;
import com.agrotech.api.appointment.domain.services.ReviewCommandService;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.ReviewEntity;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.mappers.ReviewMapper;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories.ReviewRepository;
import com.agrotech.api.shared.domain.exceptions.AdvisorNotFoundException;
import com.agrotech.api.shared.domain.exceptions.FarmerNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class ReviewCommandServiceImpl implements ReviewCommandService {
    private final ReviewRepository reviewRepository;
    private final ExternalProfilesService externalProfilesService;

    public ReviewCommandServiceImpl(ReviewRepository reviewRepository, ExternalProfilesService externalProfilesService) {
        this.reviewRepository = reviewRepository;
        this.externalProfilesService = externalProfilesService;
    }

    @Override
    public Long handle(CreateReviewCommand command) {
        var advisor = externalProfilesService.fetchAdvisorById(command.advisorId())
                .orElseThrow(() -> new AdvisorNotFoundException(command.advisorId()));
        var farmer = externalProfilesService.fetchFarmerById(command.farmerId())
                .orElseThrow(() -> new FarmerNotFoundException(command.farmerId()));
        reviewRepository.findByAdvisor_IdAndFarmer_Id(command.advisorId(), command.farmerId())
                .ifPresent(existingReview -> { throw new ReviewAlreadyExistsException(command.advisorId(), command.farmerId()); });
        if(command.rating() < 0 || command.rating() > 5) throw new InvalidRatingException(command.rating());
        var review = Review.create(command, advisor, farmer);
        var reviewEntity = reviewRepository.save(ReviewMapper.toEntity(review));
        updateAdvisorRating(command.advisorId());
        return reviewEntity.getId();
    }

    @Override
    public Optional<Review> handle(UpdateReviewCommand command) {
        var reviewEntity = reviewRepository.findById(command.id())
                .orElseThrow(() -> new ReviewNotFoundException(command.id()));
        Review.validateRating(command.rating());
        reviewEntity.update(command);
        reviewRepository.save(reviewEntity);
        updateAdvisorRating(reviewEntity.getAdvisor().getId());
        return Optional.of(ReviewMapper.toDomain(reviewEntity));
    }

    @Override
    public void handle(DeleteReviewCommand command) {
        var review = reviewRepository.findById(command.id())
                .orElseThrow(() -> new ReviewNotFoundException(command.id()));
        reviewRepository.delete(review);
    }

    private void updateAdvisorRating(Long advisorId) {
        var reviews = reviewRepository.findByAdvisor_Id(advisorId);
        if (reviews.isEmpty()) return;

        BigDecimal totalRating = BigDecimal.ZERO;

        for (ReviewEntity review : reviews) {
            totalRating = totalRating.add(BigDecimal.valueOf(review.getRating()));
        }

        BigDecimal averageRating = totalRating.divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);
        externalProfilesService.updateRating(advisorId, averageRating);
    }
}
