package com.agrotech.api.post.domain.model.aggregates;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.post.domain.model.commands.CreatePostCommand;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class PostTest {

    @Test
    public void test_create_post_with_valid_command() {
        // Arrange
        String title = "Post test";
        String description = "This is a test post description.";
        String image = "test-image.jpg";
        Long advisorId = 1L;

        CreatePostCommand command = new CreatePostCommand(advisorId, title, description, image);

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(advisorId);

        Advisor advisor = mock(Advisor.class);
        when(advisor.getId()).thenReturn(advisorId);
        when(advisor.getUser()).thenReturn(mockUser);

        // Act
        Post post = new Post(command, advisor);

        // Assert
        assertEquals(title, post.getTitle());
        assertEquals(description, post.getDescription());
        assertEquals(image, post.getImage());
        assertEquals(advisor, post.getAdvisor());
    }

    @Test
    public void test_create_post_with_null_values() {
        // Arrange
        String title = null;
        String description = null;
        String image = null;
        Long advisorId = 1L;

        CreatePostCommand command = new CreatePostCommand(advisorId, title, description, image);

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(advisorId);

        Advisor advisor = mock(Advisor.class);
        when(advisor.getId()).thenReturn(advisorId);
        when(advisor.getUser()).thenReturn(mockUser);

        // Act
        Post post = new Post(command, advisor);

        // Assert
        assertNull(post.getTitle());
        assertNull(post.getDescription());
        assertNull(post.getImage());
        assertEquals(advisor, post.getAdvisor());
    }

}