package com.agrotech.api.post.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record CreatePostResource(
        @NotNull
        String title,
        @NotNull
        String description,
        @NotNull
        MultipartFile image) {
}
