package com.agrotech.api.management.interfaces.rest.resources;

public record UpdateIotCropResource(
        Double temperature,
        Double humidity,
        Double tankCurrentVolume
) {
}
