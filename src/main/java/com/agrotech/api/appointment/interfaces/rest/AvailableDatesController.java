package com.agrotech.api.appointment.interfaces.rest;

import com.agrotech.api.appointment.domain.exceptions.AppointmentNotFoundException;
import com.agrotech.api.appointment.domain.exceptions.AvailableDateNotFoundException;
import com.agrotech.api.appointment.domain.model.commands.DeleteAvailableDateCommand;
import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.domain.model.queries.*;
import com.agrotech.api.appointment.domain.services.AvailableDateCommandService;
import com.agrotech.api.appointment.domain.services.AvailableDateQueryService;
import com.agrotech.api.appointment.interfaces.rest.resources.AvailableDateResource;
import com.agrotech.api.appointment.interfaces.rest.resources.CreateAvailableDateResource;
import com.agrotech.api.appointment.interfaces.rest.resources.UpdateAvailableDateResource;
import com.agrotech.api.appointment.interfaces.rest.transform.AvailableDateResourceFromEntityAssembler;
import com.agrotech.api.appointment.interfaces.rest.transform.CreateAvailableDateCommandFromResourceAssembler;
import com.agrotech.api.appointment.interfaces.rest.transform.UpdateAvailableDateCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RestController
@RequestMapping(value="api/v1/available_dates", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Available Dates", description = "Available Date Management Endpoints")
public class AvailableDatesController {
    private final AvailableDateCommandService availableDateCommandService;
    private final AvailableDateQueryService availableDateQueryService;

    public AvailableDatesController(AvailableDateCommandService availableDateCommandService, AvailableDateQueryService availableDateQueryService) {
        this.availableDateCommandService = availableDateCommandService;
        this.availableDateQueryService = availableDateQueryService;
    }

    @GetMapping
    public ResponseEntity<List<AvailableDateResource>> getAvailableDates(
            @RequestParam(value = "advisorId", required = false) Long advisorId,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable) {

        List<AvailableDate> availableDates;

        if (advisorId != null && isAvailable != null) {
            String status = isAvailable ? "AVAILABLE" : "UNAVAILABLE";
            var query = new GetAvailableDateByAdvisorIdAndStatusQuery(advisorId, status);
            availableDates = availableDateQueryService.handle(query);
        } else if (advisorId != null) {
            var query = new GetAvailableDatesByAdvisorIdQuery(advisorId);
            availableDates = availableDateQueryService.handle(query);
        } else if (isAvailable != null) {
            String status = isAvailable ? "AVAILABLE" : "UNAVAILABLE";
            var query = new GetAvailableDateByStatusQuery(status);
            availableDates = availableDateQueryService.handle(query);
        } else {
            var query = new GetAllAvailableDatesQuery();
            availableDates = availableDateQueryService.handle(query);
        }

        var availableDateResources = availableDates.stream()
                .map(AvailableDateResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(availableDateResources);
    }
    @GetMapping("/{id}")
    public ResponseEntity<AvailableDateResource> getAvailableDateById(@PathVariable Long id) {
        var getAvailableDateByIdQuery = new GetAvailableDateByIdQuery(id);
        var availableDate = availableDateQueryService.handle(getAvailableDateByIdQuery);
        if (availableDate.isEmpty()) throw new AvailableDateNotFoundException(id);
        var availableDateResource = AvailableDateResourceFromEntityAssembler.toResourceFromEntity(availableDate.get());
        return ResponseEntity.ok(availableDateResource);
    }

    @PostMapping
    public ResponseEntity<AvailableDateResource> createAvailableDate(@RequestBody CreateAvailableDateResource createAvailableDateResource) {
        var createAvailableDateCommand = CreateAvailableDateCommandFromResourceAssembler.toCommandFromResource(createAvailableDateResource);
        Long availableDateId = availableDateCommandService.handle(createAvailableDateCommand);
        var availableDate = availableDateQueryService.handle(new GetAvailableDateByIdQuery(availableDateId));
        if(availableDate.isEmpty()) return ResponseEntity.badRequest().build();
        var availableDateResource = AvailableDateResourceFromEntityAssembler.toResourceFromEntity(availableDate.get());
        return new ResponseEntity<>(availableDateResource, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvailableDateResource> updateAvailableDate(@PathVariable Long id, @RequestBody UpdateAvailableDateResource updateAvailableDateResource) {
        var updateAvailableDateCommand = UpdateAvailableDateCommandFromResourceAssembler.toCommandFromResource(id, updateAvailableDateResource);
        Optional<AvailableDate> availableDate = availableDateCommandService.handle(updateAvailableDateCommand);
        if(availableDate.isEmpty()) return ResponseEntity.notFound().build();
        var availableDateResource = AvailableDateResourceFromEntityAssembler.toResourceFromEntity(availableDate.get());
        return ResponseEntity.ok(availableDateResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAvailableDate(@PathVariable Long id) {
        var deleteAvailableDateCommand = new DeleteAvailableDateCommand(id);
        availableDateCommandService.handle(deleteAvailableDateCommand);
        return ResponseEntity.ok().body("Available Date with id " + id + " deleted successfully");
    }
}
