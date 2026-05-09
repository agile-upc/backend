package com.agrotech.api.post.domain.model.commands;

import org.springframework.web.multipart.MultipartFile;

public record UpdatePostCommand(Long id,
                                String title,
                                String description,
                                MultipartFile image) {
}
