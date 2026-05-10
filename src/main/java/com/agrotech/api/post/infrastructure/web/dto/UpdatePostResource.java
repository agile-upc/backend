package com.agrotech.api.post.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UpdatePostResource(
        @NotNull
        String title,
        @NotNull
        String description,
        @Schema(type = "string", format = "binary")
        MultipartFile image) {
}
