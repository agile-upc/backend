package com.agrotech.api.post.domain.services;

import com.agrotech.api.post.domain.model.aggregates.Post;
import com.agrotech.api.post.domain.model.commands.CreatePostCommand;
import com.agrotech.api.post.domain.model.commands.DeletePostCommand;
import com.agrotech.api.post.domain.model.commands.UpdatePostCommand;

import java.io.IOException;
import java.util.Optional;

public interface PostCommandService {
    Long handle(CreatePostCommand command) throws IOException;
    Optional<Post> handle(UpdatePostCommand command) throws IOException;
    void handle(DeletePostCommand command);
}
