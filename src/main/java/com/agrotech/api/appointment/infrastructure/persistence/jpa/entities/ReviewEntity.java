package com.agrotech.api.appointment.infrastructure.persistence.jpa.entities;

import com.agrotech.api.appointment.domain.model.commands.UpdateReviewCommand;
import com.agrotech.api.appointment.domain.model.entities.Review;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.AdvisorEntity;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.FarmerEntity;
import com.agrotech.api.shared.infrastructure.persistence.jpa.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private AdvisorEntity advisor;

    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private FarmerEntity farmer;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Min(0)
    @Max(5)
    @NotNull
    private Integer rating;

    public void update(UpdateReviewCommand command) {
        this.comment = command.comment();
        this.rating = command.rating();
    }
}