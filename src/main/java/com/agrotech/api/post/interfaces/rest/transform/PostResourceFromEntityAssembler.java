package com.agrotech.api.post.interfaces.rest.transform;

import com.agrotech.api.post.domain.model.aggregates.Post;
import com.agrotech.api.post.interfaces.rest.resources.PostResource;

public class PostResourceFromEntityAssembler {
    public static PostResource toResourceFromEntity(Post entity) {
        return new PostResource(
                entity.getId(),
                entity.getAdvisorId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImage());
    }
}
