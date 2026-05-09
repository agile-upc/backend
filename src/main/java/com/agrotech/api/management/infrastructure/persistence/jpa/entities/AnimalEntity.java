package com.agrotech.api.management.infrastructure.persistence.jpa.entities;

import com.agrotech.api.management.domain.model.commands.UpdateAnimalCommand;
import com.agrotech.api.management.domain.model.entities.Animal;
import com.agrotech.api.management.domain.model.valueobjects.HealthStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "animal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private Integer age;

    @NotBlank
    private String species;

    @NotBlank
    private String breed;

    @NotNull
    private Boolean gender;

    @NotNull
    @Positive
    private Float weight;

    @NotNull
    @Enumerated(EnumType.STRING)
    private HealthStatus health;

    @ManyToOne
    @JoinColumn(name = "enclosure_id")
    private EnclosureEntity enclosure;

    public void update(UpdateAnimalCommand command) {
        this.name = command.name();
        this.age = command.age();
        this.species = command.species();
        this.breed = command.breed();
        this.gender = command.gender();
        this.weight = command.weight();
        this.health = HealthStatus.valueOf(command.health().toUpperCase());
    }
}