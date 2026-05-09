package com.agrotech.api.post.application.internal.commandservices;

import com.agrotech.api.post.application.internal.outboundservices.acl.ExternalProfileService;
import com.agrotech.api.post.infrastructure.persistence.jpa.entities.PostEntity;
import com.agrotech.api.post.infrastructure.persistence.jpa.mappers.PostMapper;
import com.agrotech.api.shared.application.internal.GoogleStorageService;
import com.agrotech.api.shared.domain.exceptions.AdvisorNotFoundException;
import com.agrotech.api.post.domain.exceptions.PostNotFoundException;
import com.agrotech.api.post.domain.model.aggregates.Post;
import com.agrotech.api.post.domain.model.commands.CreatePostCommand;
import com.agrotech.api.post.domain.model.commands.DeletePostCommand;
import com.agrotech.api.post.domain.model.commands.UpdatePostCommand;
import com.agrotech.api.post.domain.services.PostCommandService;
import com.agrotech.api.post.infrastructure.persistence.jpa.repositories.PostRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class PostCommandServiceImpl implements PostCommandService {
    private final PostRepository postRepository;
    private final ExternalProfileService externalProfileService;
    private final GoogleStorageService googleStorageService;

    public PostCommandServiceImpl(PostRepository postRepository, ExternalProfileService externalProfileService,
                                  GoogleStorageService googleStorageService) {
        this.postRepository = postRepository;
        this.externalProfileService = externalProfileService;
        this.googleStorageService = googleStorageService;
    }

    @Override
    public Long handle(CreatePostCommand command) throws IOException {
        var advisor = externalProfileService.fetchAdvisorById(command.advisorId())
                .orElseThrow(() -> new AdvisorNotFoundException(command.advisorId()));
        var photoUrl = googleStorageService.uploadFile(command.image());
        var post = new Post(command, advisor, photoUrl);
        var postEntity = postRepository.save(PostMapper.toEntity(post));
        return postEntity.getId();
    }

    @Override
    public Optional<Post> handle(UpdatePostCommand command) throws IOException {
        var postEntity = postRepository.findById(command.id())
                .orElseThrow(() -> new PostNotFoundException(command.id()));
        var photoUrl = "null";
        try {
            if (command.image() != null) photoUrl = googleStorageService.uploadFile(command.image());
        } catch (IOException e) {
            // Ignore if no new photo is provided
        }
        postEntity.update(command, photoUrl);
        var updatedEntity = postRepository.save(postEntity);
        return Optional.of(PostMapper.toDomain(updatedEntity));
    }

    @Override
    public void handle(DeletePostCommand command) {
        var postEntity = postRepository.findById(command.id())
                .orElseThrow(() -> new PostNotFoundException(command.id()));
        postRepository.delete(postEntity);
    }

}
