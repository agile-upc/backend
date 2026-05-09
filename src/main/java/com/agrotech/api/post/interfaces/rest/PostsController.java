package com.agrotech.api.post.interfaces.rest;

import com.agrotech.api.post.domain.model.aggregates.Post;
import com.agrotech.api.post.domain.model.commands.DeletePostCommand;
import com.agrotech.api.post.domain.model.queries.GetAllPostsQuery;
import com.agrotech.api.post.domain.model.queries.GetPostByAdvisorIdQuery;
import com.agrotech.api.post.domain.model.queries.GetPostByIdQuery;
import com.agrotech.api.post.domain.services.PostCommandService;
import com.agrotech.api.post.domain.services.PostQueryService;
import com.agrotech.api.post.interfaces.rest.resources.CreatePostResource;
import com.agrotech.api.post.interfaces.rest.resources.PostResource;
import com.agrotech.api.post.interfaces.rest.resources.UpdatePostResource;
import com.agrotech.api.post.interfaces.rest.transform.CreatePostCommandFromResourceAssembler;
import com.agrotech.api.post.interfaces.rest.transform.PostResourceFromEntityAssembler;
import com.agrotech.api.post.interfaces.rest.transform.UpdatePostCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RestController
@RequestMapping(value="api/v1/posts", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Posts", description = "Post Management Endpoints")
public class PostsController {
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    public PostsController(PostCommandService postCommandService, PostQueryService postQueryService) {
        this.postCommandService = postCommandService;
        this.postQueryService = postQueryService;
    }

    @GetMapping
    public ResponseEntity<List<PostResource>> getPosts(
            @RequestParam(value = "advisorId", required = false) Long advisorId) {

        List<Post> posts;

        if (advisorId != null) {
            var getPostsByAdvisorIdQuery = new GetPostByAdvisorIdQuery(advisorId);
            posts = postQueryService.handle(getPostsByAdvisorIdQuery);
        } else {
            var getAllPostsQuery = new GetAllPostsQuery();
            posts = postQueryService.handle(getAllPostsQuery);
        }

        var postResources = posts.stream()
                .map(PostResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(postResources);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PostResource> getPostById(@PathVariable Long id) {
        var getPostByIdQuery = new GetPostByIdQuery(id);
        var post = postQueryService.handle(getPostByIdQuery);
        if (post.isEmpty()) return ResponseEntity.notFound().build();
        var postResource = PostResourceFromEntityAssembler.toResourceFromEntity(post.get());
        return ResponseEntity.ok(postResource);
    }

    @Operation(summary = "Create post", requestBody = @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = CreatePostResource.class))))
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PostResource> createPost(
            @ModelAttribute CreatePostResource createPostResource) throws IOException {
        var createPostCommand = CreatePostCommandFromResourceAssembler.toCommandFromResource(createPostResource);
        var postId = postCommandService.handle(createPostCommand);
        var getPostByIdQuery = new GetPostByIdQuery(postId);
        var post = postQueryService.handle(getPostByIdQuery);
        if (post.isEmpty()) return ResponseEntity.badRequest().build();
        var postResource = PostResourceFromEntityAssembler.toResourceFromEntity(post.get());
        return new ResponseEntity<>(postResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Update post", requestBody = @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = UpdatePostResource.class))))
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<PostResource> updatePost(
            @PathVariable Long id,
            @ModelAttribute UpdatePostResource updatePostResource) throws IOException {
        var updatePostCommand = UpdatePostCommandFromResourceAssembler.toCommandFromResource(id, updatePostResource);
        Optional<Post> post = postCommandService.handle(updatePostCommand);
        if (post.isEmpty()) return ResponseEntity.notFound().build();
        var postResource = PostResourceFromEntityAssembler.toResourceFromEntity(post.get());
        return ResponseEntity.ok(postResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        var deletePostCommand = new DeletePostCommand(id);
        postCommandService.handle(deletePostCommand);
        return ResponseEntity.ok("Post with id " + id + " successfully deleted");
    }
}