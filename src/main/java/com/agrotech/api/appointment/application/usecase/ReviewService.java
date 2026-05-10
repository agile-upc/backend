package com.agrotech.api.appointment.application.usecase;

import com.agrotech.api.appointment.domain.model.Review;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repository.ReviewRepository;
import com.agrotech.api.appointment.infrastructure.web.dto.CreateReviewResource;
import com.agrotech.api.appointment.infrastructure.web.dto.UpdateReviewResource;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Farmer;
import com.agrotech.api.profile.application.usecase.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProfileService profileService;

    public ReviewService(ReviewRepository reviewRepository, ProfileService profileService) {
        this.reviewRepository = reviewRepository;
        this.profileService = profileService;
    }

    public List<Review> getReviews(Long advisorId, Long farmerId) {
        if (advisorId != null && farmerId != null) {
            Review review = reviewRepository.findByAdvisor_IdAndFarmer_Id(advisorId, farmerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
            return List.of(review);
        }
        if (advisorId != null) {
            return reviewRepository.findByAdvisor_Id(advisorId);
        }
        if (farmerId != null) {
            return reviewRepository.findByFarmer_Id(farmerId);
        }
        return reviewRepository.findAll();
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    @Transactional(rollbackFor = Exception.class)
    public Review createReview(CreateReviewResource resource) {
        Farmer farmer = profileService.requireCurrentFarmerEntity();
        Advisor advisor = profileService.getAdvisorEntity(resource.advisorId());
        validateRating(resource.rating());

        reviewRepository.findByAdvisor_IdAndFarmer_Id(advisor.getId(), farmer.getId()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already exists for this advisor and farmer");
        });

        Review review = reviewRepository.save(Review.builder()
                .advisor(advisor)
                .farmer(farmer)
                .comment(resource.comment())
                .rating(resource.rating())
                .build());

        recalculateAdvisorRating(advisor.getId());
        return review;
    }

    @Transactional(rollbackFor = Exception.class)
    public Review updateReview(Long id, UpdateReviewResource resource) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        validateRating(resource.rating());
        review.setComment(resource.comment());
        review.setRating(resource.rating());
        Review savedReview = reviewRepository.save(review);
        recalculateAdvisorRating(savedReview.getAdvisor().getId());
        return savedReview;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        Long advisorId = review.getAdvisor().getId();
        reviewRepository.delete(review);
        recalculateAdvisorRating(advisorId);
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 0 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 0 and 5");
        }
    }

    private void recalculateAdvisorRating(Long advisorId) {
        List<Review> reviews = reviewRepository.findByAdvisor_Id(advisorId);
        if (reviews.isEmpty()) {
            profileService.updateAdvisorRating(advisorId, BigDecimal.ZERO);
            return;
        }

        BigDecimal total = reviews.stream()
                .map(review -> BigDecimal.valueOf(review.getRating()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = total.divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);
        profileService.updateAdvisorRating(advisorId, average);
    }
}
