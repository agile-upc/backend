package com.agrotech.api.management.infrastructure.persistence.jpa.entities;

import com.agrotech.api.management.domain.model.commands.UpdateCropCommand;
import com.agrotech.api.management.domain.model.commands.UpdateIotCropCommand;
import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.FarmerEntity;
import com.agrotech.api.shared.infrastructure.persistence.jpa.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "crop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private Double temperature;

    private Double humidity;

    @NotNull
    private Double tankMaxVolume;

    @NotNull
    private Double tankHeight;

    private Double tankCurrentVolume;

    @NotNull
    private Double temperatureMaxThreshold;

    @NotNull
    private Double humidityMinThreshold;


    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private FarmerEntity farmer;

    public void update(UpdateCropCommand command) {
        this.name = command.name();
        this.tankMaxVolume = command.tankMaxVolume();
        this.tankHeight = command.tankHeight();
        this.temperatureMaxThreshold = command.temperatureMaxThreshold();
        this.humidityMinThreshold = command.humidityMinThreshold();
    }

    public void updateIotData(UpdateIotCropCommand command) {
        this.temperature = command.temperature();
        this.humidity = command.humidity();
        this.tankCurrentVolume = command.tankCurrentVolume();
    }
}
