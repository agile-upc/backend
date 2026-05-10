package com.agrotech.api.appointment.infrastructure.web.controller;

import com.agrotech.api.appointment.infrastructure.web.dto.AvailableDateResource;
import com.agrotech.api.appointment.infrastructure.web.dto.CreateAvailableDateResource;
import com.agrotech.api.appointment.infrastructure.web.dto.UpdateAvailableDateResource;
import com.agrotech.api.appointment.application.mapper.AppointmentMapper;
import com.agrotech.api.appointment.application.usecase.AvailableDateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/available_dates", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Available Dates", description = "Available Date Management Endpoints")
public class AvailableDatesController {
    private final AvailableDateService availableDateService;
    private final AppointmentMapper appointmentMapper;

    public AvailableDatesController(AvailableDateService availableDateService, AppointmentMapper appointmentMapper) {
        this.availableDateService = availableDateService;
        this.appointmentMapper = appointmentMapper;
    }

    @GetMapping
    public ResponseEntity<List<AvailableDateResource>> getAvailableDates(
            @RequestParam(value = "advisorId", required = false) Long advisorId,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable
    ) {
        return ResponseEntity.ok(availableDateService.getAvailableDates(advisorId, isAvailable).stream().map(appointmentMapper::toAvailableDateResource).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvailableDateResource> getAvailableDateById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentMapper.toAvailableDateResource(availableDateService.getAvailableDateById(id)));
    }

    @PostMapping
    public ResponseEntity<AvailableDateResource> createAvailableDate(@RequestBody CreateAvailableDateResource createAvailableDateResource) {
        return new ResponseEntity<>(appointmentMapper.toAvailableDateResource(availableDateService.createAvailableDate(createAvailableDateResource)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvailableDateResource> updateAvailableDate(
            @PathVariable Long id,
            @RequestBody UpdateAvailableDateResource updateAvailableDateResource
    ) {
        return ResponseEntity.ok(appointmentMapper.toAvailableDateResource(availableDateService.updateAvailableDate(id, updateAvailableDateResource)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAvailableDate(@PathVariable Long id) {
        availableDateService.deleteAvailableDate(id);
        return ResponseEntity.ok("Available Date with id " + id + " deleted successfully");
    }
}
