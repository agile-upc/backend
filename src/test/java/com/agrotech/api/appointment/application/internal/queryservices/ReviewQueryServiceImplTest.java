package com.agrotech.api.appointment.application.internal.queryservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.agrotech.api.appointment.domain.model.entities.Review;
import com.agrotech.api.appointment.domain.model.queries.GetAllReviewsQuery;
import com.agrotech.api.appointment.domain.model.queries.GetReviewByAdvisorIdQuery;
import com.agrotech.api.appointment.domain.model.queries.GetReviewByFarmerIdQuery;
import com.agrotech.api.appointment.domain.model.queries.GetReviewByIdQuery;
import com.agrotech.api.appointment.domain.services.ReviewQueryService;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.ReviewEntity;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
class ReviewQueryServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewQueryServiceImpl reviewQueryService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnAllReviews() {
        // Arrange
        ReviewEntity review1 = ReviewEntity.builder()
                .comment("Excelente servicio")
                .rating(5)
                .build();

        ReviewEntity review2 = ReviewEntity.builder()
                .comment("Muy profesional")
                .rating(4)
                .build();

        when(reviewRepository.findAll()).thenReturn(List.of(review1, review2));

        // Act
        List<Review> reviews = reviewQueryService.handle(new GetAllReviewsQuery());

        // Assert
        assertEquals(2, reviews.size());
    }

    @Test
    void shouldReturnReviewById() {
        // Arrange
        Long reviewId = 1L;
        ReviewEntity review = ReviewEntity.builder()
                .id(reviewId)
                .comment("Excelente servicio")
                .rating(5)
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act
        Optional<Review> result = reviewQueryService.handle(new GetReviewByIdQuery(reviewId));

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Excelente servicio", result.get().getComment());
    }

    @Test
    void shouldReturnReviewsByAdvisorId() {
        // Arrange
        Long advisorId = 1L;
        ReviewEntity review1 = ReviewEntity.builder()
                .comment("Excelente servicio")
                .rating(5)
                .build();

        ReviewEntity review2 = ReviewEntity.builder()
                .comment("Muy profesional")
                .rating(4)
                .build();

        when(reviewRepository.findByAdvisor_Id(advisorId)).thenReturn(List.of(review1, review2));

        // Act
        List<Review> reviews = reviewQueryService.handle(new GetReviewByAdvisorIdQuery(advisorId));

        // Assert
        assertEquals(2, reviews.size());
    }

    @Test
    void shouldReturnReviewsByFarmerId() {
        // Arrange
        Long farmerId = 1L;
        ReviewEntity review = ReviewEntity.builder()
                .comment("Excelente servicio")
                .rating(5)
                .build();

        when(reviewRepository.findByFarmer_Id(farmerId)).thenReturn(List.of(review));

        // Act
        List<Review> reviews = reviewQueryService.handle(new GetReviewByFarmerIdQuery(farmerId));

        // Assert
        assertEquals(1, reviews.size());
        assertEquals("Excelente servicio", reviews.get(0).getComment());
    }
}