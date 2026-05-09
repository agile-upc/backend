package com.agrotech.api.management.infrastructure.persistence.jpa.entities;

import com.agrotech.api.management.domain.model.aggregates.Enclosure;
import com.agrotech.api.management.domain.model.commands.UpdateEnclosureCommand;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.FarmerEntity;
import com.agrotech.api.shared.infrastructure.persistence.jpa.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "enclosure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnclosureEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer capacity;

    @NotNull
    @NotBlank
    private String type;

    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private FarmerEntity farmer;

    public void update(UpdateEnclosureCommand command) {
        this.name = command.name();
        this.capacity = command.capacity();
        this.type = command.type();
    }
}
