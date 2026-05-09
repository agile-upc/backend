package com.agrotech.api.management.domain.model.entities;

import com.agrotech.api.management.domain.exceptions.IncorrectHealthStatusException;
import com.agrotech.api.management.domain.model.aggregates.Enclosure;
import com.agrotech.api.management.domain.model.commands.CreateAnimalCommand;
import com.agrotech.api.management.domain.model.commands.UpdateAnimalCommand;
import com.agrotech.api.management.domain.model.valueobjects.HealthStatus;
import lombok.Getter;

@Getter
public class Animal {
    private Long id;
    private String name;
    private Integer age;
    private String species;
    private String breed;
    private Boolean gender;
    private Float weight;
    private HealthStatus health;
    private Enclosure enclosure;

    public Animal() {}

    public Animal(Long id, String name, Integer age, String species, String breed, Boolean gender, Float weight, HealthStatus health, Enclosure enclosure) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.species = species;
        this.breed = breed;
        this.gender = gender;
        this.weight = weight;
        this.health = health;
        this.enclosure = enclosure;
    }

    public Animal(String name, Integer age, String species, String breed, Boolean gender, Float weight, HealthStatus health, Enclosure enclosure) {
        this.name = name;
        this.age = age;
        this.species = species;
        this.breed = breed;
        this.gender = gender;
        this.weight = weight;
        this.health = health;
        this.enclosure = enclosure;
    }


    public static Animal create(CreateAnimalCommand command, Enclosure enclosure) {
        if (!command.health().matches("^(?i)(HEALTHY|SICK|DEAD|UNKNOWN)$")) {
            throw new IncorrectHealthStatusException(command.health());
        }

        return new Animal(
                command.name(),
                command.age(),
                command.species(),
                command.breed(),
                command.gender(),
                command.weight(),
                HealthStatus.valueOf(command.health().toUpperCase()),
                enclosure
        );
    }

    public static void validateHealthStatus(String healthStatus) {
        if (!healthStatus.matches("^(?i)(HEALTHY|SICK|DEAD|UNKNOWN)$")) {
            throw new IncorrectHealthStatusException(healthStatus);
        }
    }

    public Long getEnclosureId() {
        return this.enclosure.getId();
    }

    public String getHealthStatus() {
        return this.health.toString();
    }
}