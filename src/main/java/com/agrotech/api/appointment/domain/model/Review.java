package com.agrotech.api.appointment.domain.model;

import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Farmer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private Advisor advisor;

    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private Farmer farmer;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String comment;

    @Min(0)
    @Max(5)
    @NotNull
    private Integer rating;
}
