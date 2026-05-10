package com.agrotech.api.post.infrastructure.web.controller;

import com.agrotech.api.post.infrastructure.web.dto.CreatePostResource;
import com.agrotech.api.post.infrastructure.web.dto.PostResource;
import com.agrotech.api.post.infrastructure.web.dto.UpdatePostResource;
import com.agrotech.api.post.application.mapper.PostMapper;
import com.agrotech.api.post.application.usecase.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/posts", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Posts", description = "Post Management Endpoints")
public class PostsController {
    private final PostService postService;
    private final PostMapper postMapper;

    public PostsController(PostService postService, PostMapper postMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
    }

    @GetMapping
    public ResponseEntity<List<PostResource>> getPosts() {
        return ResponseEntity.ok(postService.getPosts().stream().map(postMapper::toPostResource).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResource> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postMapper.toPostResource(postService.getPostById(id)));
    }

    @Operation(summary = "Create post", requestBody = @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = CreatePostResource.class))))
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PostResource> createPost(@ModelAttribute CreatePostResource createPostResource) throws IOException {
        return new ResponseEntity<>(postMapper.toPostResource(postService.createPost(createPostResource)), HttpStatus.CREATED);
    }

    @Operation(summary = "Update post", requestBody = @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = UpdatePostResource.class))))
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<PostResource> updatePost(
            @PathVariable Long id,
            @ModelAttribute UpdatePostResource updatePostResource
    ) throws IOException {
        return ResponseEntity.ok(postMapper.toPostResource(postService.updatePost(id, updatePostResource)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Post with id " + id + " successfully deleted");
    }
}
