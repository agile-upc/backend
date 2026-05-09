package com.agrotech.api.post.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.agrotech.api.post.application.internal.outboundservices.acl.ExternalProfileService;
import com.agrotech.api.post.domain.model.commands.CreatePostCommand;
import com.agrotech.api.post.infrastructure.persistence.jpa.entities.PostEntity;
import com.agrotech.api.post.infrastructure.persistence.jpa.repositories.PostRepository;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@ActiveProfiles("test")
class PostCommandServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ExternalProfileService externalProfileService;

    @InjectMocks
    private PostCommandServiceImpl postCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Creating a post with valid advisor ID returns the post ID
    @Test
    public void test_handle_create_post_command_with_valid_advisor_returns_post_id() {
        // Arrange
        Long advisorId = 1L;
        Long expectedPostId = 10L;
        String title = "Test Title";
        String description = "Test Description";
        String image = "test-image.jpg";

        // Mock Advisor
        Advisor advisor = mock(Advisor.class);
        when(advisor.getId()).thenReturn(advisorId);

        // Command with advisorId and post details
        CreatePostCommand command = new CreatePostCommand(advisorId, title, description, image);

        // Mock PostEntity returned by repository after save, with ID set
        PostEntity mockPostEntity = mock(PostEntity.class);
        when(mockPostEntity.getId()).thenReturn(expectedPostId);

        // External service returns advisor correctly
        when(externalProfileService.fetchAdvisorById(advisorId)).thenReturn(Optional.of(advisor));

        // Repository saves PostEntity and returns the saved entity with ID
        when(postRepository.save(any(PostEntity.class))).thenReturn(mockPostEntity);

        // Act
        Long actualPostId = postCommandService.handle(command);

        // Assert
        assertEquals(expectedPostId, actualPostId);

        verify(externalProfileService).fetchAdvisorById(advisorId);
        verify(postRepository).save(any(PostEntity.class));
    }
}
