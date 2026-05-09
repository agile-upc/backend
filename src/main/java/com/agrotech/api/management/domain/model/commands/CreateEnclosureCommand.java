package com.agrotech.api.management.domain.model.commands;

public record CreateEnclosureCommand(String name, Integer capacity, String type, Long farmerId) {
}
