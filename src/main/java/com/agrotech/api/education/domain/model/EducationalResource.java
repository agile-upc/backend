package com.agrotech.api.education.domain.model;

import com.agrotech.api.education.domain.valueobject.EducationalResourceType;
import com.agrotech.api.shared.infrastructure.persistence.jpa.base.AuditableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "educational_resource")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationalResource extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EducationalResourceType type;

    @Column(nullable = false)
    private String sourceName;

    @Column(nullable = false, unique = true, length = 768)
    private String sourceUrl;

    @Column(length = 1024)
    private String downloadUrl;

    @Column(length = 1024)
    private String thumbnailUrl;

    private LocalDate publishedAt;

    @ElementCollection
    @CollectionTable(name = "educational_resource_topic", joinColumns = @JoinColumn(name = "resource_id"))
    @Column(name = "topic", nullable = false)
    @Builder.Default
    private List<String> topics = new ArrayList<>();
}
