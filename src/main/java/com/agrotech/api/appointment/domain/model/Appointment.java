package com.agrotech.api.appointment.domain.model;

import com.agrotech.api.appointment.domain.valueobject.AppointmentStatus;
import com.agrotech.api.profile.domain.model.Farmer;
import com.agrotech.api.shared.infrastructure.persistence.jpa.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    private String meetingUrl;

    @Getter
    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private Farmer farmer;

    @Getter
    @OneToOne
    @JoinColumn(name = "available_date_id")
    private AvailableDate availableDate;
}
