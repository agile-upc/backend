package com.agrotech.api.appointment.infrastructure.persistence.jpa.entities;

import com.agrotech.api.appointment.domain.model.aggregates.Appointment;
import com.agrotech.api.appointment.domain.model.commands.UpdateAppointmentCommand;
import com.agrotech.api.appointment.domain.model.valueobjects.AppointmentStatus;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.FarmerEntity;
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
public class AppointmentEntity extends AuditableEntity {
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
    private FarmerEntity farmer;

    @Getter
    @OneToOne
    @JoinColumn(name = "available_date_id")
    private AvailableDateEntity availableDate;

    public void update(UpdateAppointmentCommand command){
        this.message = command.message();
        this.status = AppointmentStatus.valueOf(command.status());
    }
}