package com.agrotech.api.management.domain.model.commands;

public record UpdateIotCropCommand(
        Long cropId,
        Double temperature,
        Double humidity,
        Double tankCurrentVolume
) {
}
