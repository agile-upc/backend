package com.agrotech.api.management.domain.model.commands;

public record UpdateEnclosureCommand(Long enclosureId, String name, Integer capacity, String type) {
}
