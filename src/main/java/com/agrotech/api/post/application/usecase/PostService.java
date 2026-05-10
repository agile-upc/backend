package com.agrotech.api.post.application.usecase;

import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.post.infrastructure.web.dto.CreatePostResource;
import com.agrotech.api.post.infrastructure.web.dto.UpdatePostResource;
import com.agrotech.api.post.application.mapper.PostMapper;
import com.agrotech.api.post.domain.model.Post;
import com.agrotech.api.post.infrastructure.persistence.jpa.repository.PostRepository;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.application.usecase.ProfileService;
import com.agrotech.api.shared.infrastructure.storage.GoogleStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final ProfileService profileService;
    private final AuthenticatedUserService authenticatedUserService;
    private final GoogleStorageService googleStorageService;
    private final PostMapper postMapper;

    public PostService(
            PostRepository postRepository,
            ProfileService profileService,
            AuthenticatedUserService authenticatedUserService,
            GoogleStorageService googleStorageService,
            PostMapper postMapper
    ) {
        this.postRepository = postRepository;
        this.profileService = profileService;
        this.authenticatedUserService = authenticatedUserService;
        this.googleStorageService = googleStorageService;
        this.postMapper = postMapper;
    }

    public List<Post> getPosts() {
        AuthenticatedUser authenticatedUser = authenticatedUserService.getCurrentUser();

        if (authenticatedUser.role() == UserRole.ADVISOR) {
            return postRepository.findByAdvisor_Id(authenticatedUser.advisorId());
        }

        return postRepository.findAllByOrderByUpdatedAtDesc();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    @Transactional
    public Post createPost(CreatePostResource resource) throws IOException {
        Advisor advisor = profileService.requireCurrentAdvisorEntity();
        Post post = postMapper.toPost(resource, advisor, uploadIfPresent(resource.image()));
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long id, UpdatePostResource resource) throws IOException {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        postMapper.updatePost(post, resource, uploadIfPresent(resource.image()));
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        postRepository.delete(post);
    }

    private String uploadIfPresent(org.springframework.web.multipart.MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return googleStorageService.uploadFile(file);
    }
}
