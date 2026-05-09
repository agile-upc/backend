package com.agrotech.api.management.domain.model.aggregates;

import com.agrotech.api.management.domain.model.commands.CreateCropCommand;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import lombok.Getter;

@Getter
public class Crop {
    private Long id;
    private String name;
    private Double temperature;
    private Double humidity;
    private Double tankMaxVolume;
    private Double tankHeight;
    private Double tankCurrentVolume;
    private Double temperatureMaxThreshold;
    private Double humidityMinThreshold;
    private Farmer farmer;

    public Crop() {
    }

    public Crop(Long id, String name, Double temperature, Double humidity, Double tankMaxVolume,
                Double tankHeight, Double tankCurrentVolume, Double temperatureMaxThreshold,
                Double humidityMinThreshold, Farmer farmer) {
        this.id = id;
        this.name = name;
        this.temperature = temperature;
        this.humidity = humidity;
        this.tankMaxVolume = tankMaxVolume;
        this.tankHeight = tankHeight;
        this.tankCurrentVolume = tankCurrentVolume;
        this.temperatureMaxThreshold = temperatureMaxThreshold;
        this.humidityMinThreshold = humidityMinThreshold;
        this.farmer = farmer;
    }

    public Crop(CreateCropCommand command, Farmer farmer) {
        this.name = command.name();
        this.temperature = 0.0;
        this.humidity = 0.0;
        this.tankMaxVolume = command.tankMaxVolume();
        this.tankHeight = command.tankHeight();
        this.tankCurrentVolume = 0.0;
        this.temperatureMaxThreshold = command.temperatureMaxThreshold();
        this.humidityMinThreshold = command.humidityMinThreshold();
        this.farmer = farmer;
    }

}
