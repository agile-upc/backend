package com.agrotech.api.management.domain.model.commands;

public record CreateCropCommand (
        String name,
        Double tankMaxVolume,
        Double tankHeight,
        Double temperatureMaxThreshold,
        Double humidityMinThreshold,
        Long farmerId
) {}
