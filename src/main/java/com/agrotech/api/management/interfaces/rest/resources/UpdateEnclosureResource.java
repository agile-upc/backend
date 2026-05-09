package com.agrotech.api.management.interfaces.rest.resources;

public record UpdateEnclosureResource(
        String name,
        Integer capacity,
        String type
) {}
