package com.agrotech.api.post.domain.exceptions;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long id) {
        super("Post with id " + id + " not found");
    }
}
