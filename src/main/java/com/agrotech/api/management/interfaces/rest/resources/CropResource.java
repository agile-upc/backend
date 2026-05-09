package com.agrotech.api.management.interfaces.rest.resources;

public record CropResource(
        Long id,
        String name,
        Double temperature,
        Double humidity,
        Double tankMaxVolume,
        Double tankHeight,
        Double tankCurrentVolume,
        Double temperatureMaxThreshold,
        Double humidityMinThreshold
) {
}
