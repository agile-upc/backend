package com.agrotech.api.management.interfaces.rest.resources;

public record CreateCropResource(
        String name,
        Double tankMaxVolume,
        Double tankHeight,
        Double temperatureMaxThreshold,
        Double humidityMinThreshold,
        Long farmerId
) {
}
