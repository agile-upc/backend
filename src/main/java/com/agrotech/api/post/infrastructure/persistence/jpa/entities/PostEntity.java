package com.agrotech.api.post.infrastructure.persistence.jpa.entities;

import com.agrotech.api.post.domain.model.aggregates.Post;
import com.agrotech.api.post.domain.model.commands.UpdatePostCommand;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.AdvisorEntity;
import com.agrotech.api.shared.infrastructure.persistence.jpa.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String image;

    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private AdvisorEntity advisor;

    public void update(UpdatePostCommand command, String imageUrl) {
        this.title = command.title();
        this.description = command.description();
        if (!Objects.equals(imageUrl, "null")) this.image = imageUrl;
    }
}