package com.agrotech.api.appointment.interfaces.rest;

import com.agrotech.api.appointment.domain.exceptions.AppointmentNotFoundException;
import com.agrotech.api.appointment.domain.model.aggregates.Appointment;
import com.agrotech.api.appointment.domain.model.commands.DeleteAppointmentCommand;
import com.agrotech.api.appointment.domain.model.queries.*;
import com.agrotech.api.appointment.domain.services.AppointmentCommandService;
import com.agrotech.api.appointment.domain.services.AppointmentQueryService;
import com.agrotech.api.appointment.interfaces.rest.resources.AppointmentResource;
import com.agrotech.api.appointment.interfaces.rest.resources.CreateAppointmentResource;
import com.agrotech.api.appointment.interfaces.rest.resources.UpdateAppointmentResource;
import com.agrotech.api.appointment.interfaces.rest.transform.AppointmentResourceFromEntityAssembler;
import com.agrotech.api.appointment.interfaces.rest.transform.CreateAppointmentCommandFromResourceAssembler;
import com.agrotech.api.appointment.interfaces.rest.transform.UpdateAppointmentCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RestController
@RequestMapping(value="api/v1/appointments", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Appointments", description = "Appointment Management Endpoints")
public class AppointmentsController {
    private final AppointmentCommandService appointmentCommandService;
    private final AppointmentQueryService appointmentQueryService;

    public AppointmentsController(AppointmentCommandService appointmentCommandService, AppointmentQueryService appointmentQueryService) {
        this.appointmentCommandService = appointmentCommandService;
        this.appointmentQueryService = appointmentQueryService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResource>> getAppointments(
            @RequestParam(value = "farmerId", required = false) Long farmerId,
            @RequestParam(value = "advisorId", required = false) Long advisorId) {

        List<Appointment> appointments;

        if (farmerId != null && advisorId != null) {
            var query = new GetAppointmentsByAdvisorIdAndFarmerIdQuery(advisorId, farmerId);
            appointments = appointmentQueryService.handle(query);
        } else if (farmerId != null) {
            var query = new GetAppointmentsByFarmerIdQuery(farmerId);
            appointments = appointmentQueryService.handle(query);
        } else if (advisorId != null) {
            var query = new GetAppointmentsByAdvisorIdQuery(advisorId);
            appointments = appointmentQueryService.handle(query);
        } else {
            var query = new GetAllAppointmentsQuery();
            appointments = appointmentQueryService.handle(query);
        }

        var appointmentResources = appointments.stream()
                .map(AppointmentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(appointmentResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResource> getAppointmentById(@PathVariable Long id) {
        var getAppointmentByIdQuery = new GetAppointmentByIdQuery(id);
        var appointment = appointmentQueryService.handle(getAppointmentByIdQuery);
        if (appointment.isEmpty()) throw new AppointmentNotFoundException(id);
        var appointmentResource = AppointmentResourceFromEntityAssembler.toResourceFromEntity(appointment.get());
        return ResponseEntity.ok(appointmentResource);
    }

    @PostMapping
    public ResponseEntity<AppointmentResource> createAppointment(@RequestBody CreateAppointmentResource createAppointmentResource) {
        var createAppointmentCommand = CreateAppointmentCommandFromResourceAssembler.toCommandFromResource(createAppointmentResource);
        Long appointmentId = appointmentCommandService.handle(createAppointmentCommand);
        var appointment = appointmentQueryService.handle(new GetAppointmentByIdQuery(appointmentId));
        if (appointment.isEmpty()) return ResponseEntity.badRequest().build();
        var appointmentResource = AppointmentResourceFromEntityAssembler.toResourceFromEntity(appointment.get());
        return new ResponseEntity<>(appointmentResource, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResource> updateAppointment(@PathVariable Long id, @RequestBody UpdateAppointmentResource updateAppointmentResource) {
        var updateAppointmentCommand = UpdateAppointmentCommandFromResourceAssembler.toCommandFromResource(id, updateAppointmentResource);
        Optional<Appointment> appointment = appointmentCommandService.handle(updateAppointmentCommand);
        if (appointment.isEmpty()) return ResponseEntity.notFound().build();
        var appointmentResource = AppointmentResourceFromEntityAssembler.toResourceFromEntity(appointment.get());
        return ResponseEntity.ok(appointmentResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        var deleteAppointmentCommand = new DeleteAppointmentCommand(id);
        appointmentCommandService.handle(deleteAppointmentCommand);
        return ResponseEntity.ok().body("Appointment with id " + id + " deleted successfully");
    }
}
