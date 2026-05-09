package com.agrotech.api.appointment.infrastructure.persistence.jpa.entities;

import com.agrotech.api.appointment.domain.model.commands.UpdateAvailableDateCommand;
import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.domain.model.valueobjects.AvailableDateStatus;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.AdvisorEntity;
import com.agrotech.api.shared.infrastructure.persistence.jpa.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "available_date")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private AdvisorEntity advisor;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduledDate;

    @Column(nullable = false)
    private String startTime;

    @Column(nullable = false)
    private String endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailableDateStatus status;

    public void update(UpdateAvailableDateCommand command) {
        this.scheduledDate = command.scheduledDate();
        this.startTime = command.startTime();
        this.endTime = command.endTime();
    }

    public void updateStatus(String status) {
        this.status = AvailableDateStatus.valueOf(status);
    }
}