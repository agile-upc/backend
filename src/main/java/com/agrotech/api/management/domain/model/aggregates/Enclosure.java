package com.agrotech.api.management.domain.model.aggregates;

import com.agrotech.api.management.domain.model.commands.CreateEnclosureCommand;
import com.agrotech.api.management.domain.model.commands.UpdateEnclosureCommand;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import lombok.Getter;

@Getter
public class Enclosure {
    private Long id;
    private String name;
    private Integer capacity;
    private String type;
    private Farmer farmer;

    public Enclosure() {}

    public Enclosure(Long id, String name, Integer capacity, String type, Farmer farmer) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.type = type;
        this.farmer = farmer;
    }

    public Enclosure(CreateEnclosureCommand command, Farmer farmer) {
        this.name = command.name();
        this.capacity = command.capacity();
        this.type = command.type();
        this.farmer = farmer;
    }

    public Long getFarmerId() {
        return this.farmer.getId();
    }
}
