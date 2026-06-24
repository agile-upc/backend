package com.agrotech.api.post.domain.model;

import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.shared.infrastructure.persistence.jpa.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String description;

    private String image;

    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private Advisor advisor;
}
