package com.agrotech.api.management.domain.model.commands;

public record UpdateCropCommand(
        Long cropId,
        String name,
        Double tankMaxVolume,
        Double tankHeight,
        Double temperatureMaxThreshold,
        Double humidityMinThreshold
) {}
