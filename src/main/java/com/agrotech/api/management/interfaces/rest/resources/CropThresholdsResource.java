package com.agrotech.api.management.interfaces.rest.resources;

public record CropThresholdsResource(
        Double temperatureMaxThreshold,
        Double humidityMinThreshold,
        Double tankMaxVolume,
        Double tankHeight
) {
}
