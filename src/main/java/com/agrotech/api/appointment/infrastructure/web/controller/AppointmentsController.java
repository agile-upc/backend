package com.agrotech.api.appointment.infrastructure.web.controller;

import com.agrotech.api.appointment.infrastructure.web.dto.AppointmentResource;
import com.agrotech.api.appointment.infrastructure.web.dto.CreateAppointmentResource;
import com.agrotech.api.appointment.infrastructure.web.dto.UpdateAppointmentResource;
import com.agrotech.api.appointment.application.mapper.AppointmentMapper;
import com.agrotech.api.appointment.application.usecase.AppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/appointments", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Appointments", description = "Appointment Management Endpoints")
public class AppointmentsController {
    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    public AppointmentsController(AppointmentService appointmentService, AppointmentMapper appointmentMapper) {
        this.appointmentService = appointmentService;
        this.appointmentMapper = appointmentMapper;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResource>> getAppointments() {
        return ResponseEntity.ok(appointmentService.getAppointments().stream().map(appointmentMapper::toAppointmentResource).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResource> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentMapper.toAppointmentResource(appointmentService.getAppointmentById(id)));
    }

    @PostMapping
    public ResponseEntity<AppointmentResource> createAppointment(@RequestBody CreateAppointmentResource createAppointmentResource) {
        return new ResponseEntity<>(appointmentMapper.toAppointmentResource(appointmentService.createAppointment(createAppointmentResource)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResource> updateAppointment(
            @PathVariable Long id,
            @RequestBody UpdateAppointmentResource updateAppointmentResource
    ) {
        return ResponseEntity.ok(appointmentMapper.toAppointmentResource(appointmentService.updateAppointment(id, updateAppointmentResource)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok("Appointment with id " + id + " deleted successfully");
    }
}
