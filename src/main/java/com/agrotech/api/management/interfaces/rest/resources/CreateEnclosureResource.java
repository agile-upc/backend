package com.agrotech.api.management.interfaces.rest.resources;


public record CreateEnclosureResource(
        String name,
        Integer capacity,
        String type,
        Long farmerId
) {}
