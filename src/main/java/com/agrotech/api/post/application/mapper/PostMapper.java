package com.agrotech.api.post.application.mapper;

import com.agrotech.api.post.infrastructure.web.dto.CreatePostResource;
import com.agrotech.api.post.infrastructure.web.dto.PostResource;
import com.agrotech.api.post.infrastructure.web.dto.UpdatePostResource;
import com.agrotech.api.post.domain.model.Post;
import com.agrotech.api.profile.domain.model.Advisor;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {
    public PostResource toPostResource(Post post) {
        return new PostResource(
                post.getId(),
                post.getAdvisor().getId(),
                post.getTitle(),
                post.getDescription(),
                post.getImage()
        );
    }

    public Post toPost(CreatePostResource resource, Advisor advisor, String image) {
        return Post.builder()
                .advisor(advisor)
                .title(resource.title())
                .description(resource.description())
                .image(image)
                .build();
    }

    public void updatePost(Post post, UpdatePostResource resource, String uploadedImage) {
        post.setTitle(resource.title());
        post.setDescription(resource.description());
        if (uploadedImage != null) {
            post.setImage(uploadedImage);
        }
    }
}
