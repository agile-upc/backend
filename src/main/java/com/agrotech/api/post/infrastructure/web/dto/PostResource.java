package com.agrotech.api.post.infrastructure.web.dto;

public record PostResource(Long id, Long advisorId, String title, String description, String image) {
}
