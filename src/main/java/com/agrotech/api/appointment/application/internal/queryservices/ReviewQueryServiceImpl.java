package com.agrotech.api.appointment.application.internal.queryservices;

import com.agrotech.api.appointment.domain.model.entities.Review;
import com.agrotech.api.appointment.domain.model.queries.*;
import com.agrotech.api.appointment.domain.services.ReviewQueryService;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.mappers.ReviewMapper;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewQueryServiceImpl implements ReviewQueryService {
    private final ReviewRepository reviewRepository;

    public ReviewQueryServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<Review> handle(GetAllReviewsQuery query) {
        return reviewRepository.findAll()
                .stream()
                .map(ReviewMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Review> handle(GetReviewByIdQuery query) {
        return reviewRepository.findById(query.id())
                .map(ReviewMapper::toDomain);
    }

    @Override
    public List<Review> handle(GetReviewByAdvisorIdQuery query) {
        return reviewRepository.findByAdvisor_Id(query.advisorId())
                .stream()
                .map(ReviewMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> handle(GetReviewByFarmerIdQuery query) {
        return reviewRepository.findByFarmer_Id(query.farmerId())
                .stream()
                .map(ReviewMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Review> handle(GetReviewByAdvisorIdAndFarmerIdQuery query) {
        return reviewRepository.findByAdvisor_IdAndFarmer_Id(query.advisorId(), query.farmerId())
                .map(ReviewMapper::toDomain);
    }
}
