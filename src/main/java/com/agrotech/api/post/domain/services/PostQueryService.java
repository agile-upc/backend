package com.agrotech.api.post.domain.services;

import com.agrotech.api.post.domain.model.aggregates.Post;
import com.agrotech.api.post.domain.model.queries.GetAllPostsQuery;
import com.agrotech.api.post.domain.model.queries.GetPostByAdvisorIdQuery;
import com.agrotech.api.post.domain.model.queries.GetPostByIdQuery;

import java.util.List;
import java.util.Optional;

public interface PostQueryService {
    List<Post> handle(GetAllPostsQuery query);
    Optional<Post> handle(GetPostByIdQuery query);
    List<Post> handle(GetPostByAdvisorIdQuery query);
}
