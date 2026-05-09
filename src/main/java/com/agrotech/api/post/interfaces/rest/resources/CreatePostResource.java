package com.agrotech.api.post.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record CreatePostResource(
        @NotNull
        Long advisorId,
        @NotNull
        String title,
        @NotNull
        String description,
        @NotNull
        MultipartFile image) {
}
