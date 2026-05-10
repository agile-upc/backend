package com.agrotech.api.appointment.infrastructure.web.controller;

import com.agrotech.api.appointment.infrastructure.web.dto.CreateReviewResource;
import com.agrotech.api.appointment.infrastructure.web.dto.ReviewResource;
import com.agrotech.api.appointment.infrastructure.web.dto.UpdateReviewResource;
import com.agrotech.api.appointment.application.mapper.AppointmentMapper;
import com.agrotech.api.appointment.application.usecase.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/reviews", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Reviews", description = "Review Management Endpoints")
public class ReviewsController {
    private final ReviewService reviewService;
    private final AppointmentMapper appointmentMapper;

    public ReviewsController(ReviewService reviewService, AppointmentMapper appointmentMapper) {
        this.reviewService = reviewService;
        this.appointmentMapper = appointmentMapper;
    }

    @GetMapping
    public ResponseEntity<List<ReviewResource>> getReviews(
            @RequestParam(value = "advisorId", required = false) Long advisorId,
            @RequestParam(value = "farmerId", required = false) Long farmerId
    ) {
        return ResponseEntity.ok(reviewService.getReviews(advisorId, farmerId).stream().map(appointmentMapper::toReviewResource).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResource> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentMapper.toReviewResource(reviewService.getReviewById(id)));
    }

    @PostMapping
    public ResponseEntity<ReviewResource> createReview(@RequestBody CreateReviewResource createReviewResource) {
        return new ResponseEntity<>(appointmentMapper.toReviewResource(reviewService.createReview(createReviewResource)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResource> updateReview(
            @PathVariable Long id,
            @RequestBody UpdateReviewResource updateReviewResource
    ) {
        return ResponseEntity.ok(appointmentMapper.toReviewResource(reviewService.updateReview(id, updateReviewResource)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review with id " + id + " deleted successfully");
    }
}
