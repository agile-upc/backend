package com.agrotech.api.management.interfaces.rest.resources;

public record UpdateCropResource(
        String name,
        Double tankMaxVolume,
        Double tankHeight,
        Double temperatureMaxThreshold,
        Double humidityMinThreshold
) {
}
