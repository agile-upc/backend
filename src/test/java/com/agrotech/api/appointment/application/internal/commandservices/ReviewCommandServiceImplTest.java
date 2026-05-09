package com.agrotech.api.appointment.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalProfilesService;
import com.agrotech.api.appointment.domain.model.commands.CreateReviewCommand;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.ReviewEntity;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories.ReviewRepository;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.AdvisorMapper;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.FarmerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
class ReviewCommandServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ExternalProfilesService externalProfilesService;

    @InjectMocks
    private ReviewCommandServiceImpl reviewCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_handle_create_review_command_success() {
        // Arrange
        Long advisorId = 1L;
        Long farmerId = 2L;
        String comment = "Great advice";
        Integer rating = 5;
        Long reviewId = 1L;

        CreateReviewCommand command = new CreateReviewCommand(advisorId, farmerId, comment, rating);

        Advisor advisor = mock(Advisor.class);
        when(advisor.getId()).thenReturn(advisorId);

        Farmer farmer = mock(Farmer.class);
        when(farmer.getId()).thenReturn(farmerId);

        // Creamos un ReviewEntity para simular la entidad persistida
        ReviewEntity reviewEntity = ReviewEntity.builder()
                .id(reviewId)
                .advisor(AdvisorMapper.toEntity(advisor))
                .farmer(FarmerMapper.toEntity(farmer))
                .comment(comment)
                .rating(rating)
                .build();

        // Simulamos las respuestas
        when(externalProfilesService.fetchAdvisorById(advisorId)).thenReturn(Optional.of(advisor));
        when(externalProfilesService.fetchFarmerById(farmerId)).thenReturn(Optional.of(farmer));
        when(reviewRepository.findByAdvisor_IdAndFarmer_Id(advisorId, farmerId)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(reviewEntity);
        when(reviewRepository.findByAdvisor_Id(advisorId)).thenReturn(List.of(reviewEntity));
        doNothing().when(externalProfilesService).updateRating(eq(advisorId), any(BigDecimal.class));

        // Act
        Long result = reviewCommandService.handle(command);

        // Assert
        assertEquals(reviewId, result);

        // Verificaciones
        verify(reviewRepository).findByAdvisor_IdAndFarmer_Id(advisorId, farmerId);
        verify(externalProfilesService).fetchAdvisorById(advisorId);
        verify(externalProfilesService).fetchFarmerById(farmerId);
        verify(reviewRepository).save(any(ReviewEntity.class));
        verify(reviewRepository).findByAdvisor_Id(advisorId);
        verify(externalProfilesService).updateRating(eq(advisorId), any(BigDecimal.class));
    }
}