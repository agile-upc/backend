package com.agrotech.api.post.domain.model.commands;

import org.springframework.web.multipart.MultipartFile;

public record CreatePostCommand(Long advisorId, String title, String description, MultipartFile image) {
}
