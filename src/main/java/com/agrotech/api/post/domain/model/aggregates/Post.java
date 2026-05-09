package com.agrotech.api.post.domain.model.aggregates;

import com.agrotech.api.post.domain.model.commands.CreatePostCommand;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import lombok.Getter;

@Getter
public class Post {
    private Long id;
    private String title;
    private String description;
    private String image;
    private Advisor advisor;

    public Post() {}

    public Post(CreatePostCommand command, Advisor advisor, String imageUrl) {
        this.title = command.title();
        this.description = command.description();
        this.image = imageUrl;
        this.advisor = advisor;
    }

    public Post(Long id, String title, String description, String image, Advisor advisor) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.image = image;
        this.advisor = advisor;
    }

    public Long getAdvisorId() {
        return this.advisor.getId();
    }
}