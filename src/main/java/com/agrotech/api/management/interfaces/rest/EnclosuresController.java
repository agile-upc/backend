package com.agrotech.api.management.interfaces.rest;

import com.agrotech.api.management.domain.model.aggregates.Enclosure;
import com.agrotech.api.management.domain.model.commands.DeleteEnclosureCommand;
import com.agrotech.api.management.domain.model.queries.GetAllEnclosuresByFarmerIdQuery;
import com.agrotech.api.management.domain.model.queries.GetAllEnclosuresQuery;
import com.agrotech.api.management.domain.model.queries.GetEnclosureByIdQuery;
import com.agrotech.api.management.domain.services.AnimalQueryService;
import com.agrotech.api.management.domain.services.EnclosureCommandService;
import com.agrotech.api.management.domain.services.EnclosureQueryService;
import com.agrotech.api.management.interfaces.rest.resources.CreateEnclosureResource;
import com.agrotech.api.management.interfaces.rest.resources.EnclosureResource;
import com.agrotech.api.management.interfaces.rest.resources.UpdateEnclosureResource;
import com.agrotech.api.management.interfaces.rest.transform.CreateEnclosureCommandFromResourceAssembler;
import com.agrotech.api.management.interfaces.rest.transform.EnclosureResourceFromEntityAssembler;
import com.agrotech.api.management.interfaces.rest.transform.UpdateEnclosureCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.Optional;

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RestController
@RequestMapping(value = "/api/v1/enclosures", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Enclosures", description = "Enclosure Management Endpoints")
public class EnclosuresController {
    private final EnclosureCommandService enclosureCommandService;
    private final EnclosureQueryService enclosureQueryService;

    public EnclosuresController(EnclosureCommandService enclosureCommandService, EnclosureQueryService enclosureQueryService, AnimalQueryService animalQueryService){
        this.enclosureCommandService = enclosureCommandService;
        this.enclosureQueryService = enclosureQueryService;
    }

    @GetMapping
    public ResponseEntity<List<EnclosureResource>> getEnclosures(
            @RequestParam(value = "farmerId", required = false) Long farmerId
    ) {
        List<Enclosure> enclosures;
        if (farmerId != null) {
            var getAllEnclosuresByFarmerIdQuery = new GetAllEnclosuresByFarmerIdQuery(farmerId);
            enclosures = enclosureQueryService.handle(getAllEnclosuresByFarmerIdQuery);
        } else {
            var getAllEnclosuresQuery = new GetAllEnclosuresQuery();
            enclosures = enclosureQueryService.handle(getAllEnclosuresQuery);
        }
        var enclosureResources = enclosures.stream().map(EnclosureResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(enclosureResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnclosureResource> getEnclosureById(@PathVariable Long id) {
        var getEnclosureByIdQuery = new GetEnclosureByIdQuery(id);
        var enclosure = enclosureQueryService.handle(getEnclosureByIdQuery);
        if (enclosure.isEmpty()) return ResponseEntity.notFound().build();
        var enclosureResource = EnclosureResourceFromEntityAssembler.toResourceFromEntity(enclosure.get());
        return ResponseEntity.ok(enclosureResource);
    }

    @PostMapping
    public ResponseEntity<EnclosureResource> createEnclosure(@RequestBody CreateEnclosureResource createEnclosureResource) {
        var createEnclosureCommand = CreateEnclosureCommandFromResourceAssembler.toCommandFromResource(createEnclosureResource);
        Long enclosureId = enclosureCommandService.handle(createEnclosureCommand);
        var enclosure = enclosureQueryService.handle(new GetEnclosureByIdQuery(enclosureId));
        if (enclosure.isEmpty()) return ResponseEntity.badRequest().build();
        var enclosureResource = EnclosureResourceFromEntityAssembler.toResourceFromEntity(enclosure.get());
        return new ResponseEntity<>(enclosureResource, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnclosureResource> updateEnclosure(@PathVariable Long id, @RequestBody UpdateEnclosureResource updateEnclosureResource) {
        var updateEnclosureCommand = UpdateEnclosureCommandFromResourceAssembler.toCommandFromResource(id, updateEnclosureResource);
        Optional<Enclosure> enclosure = enclosureCommandService.handle(updateEnclosureCommand);
        if (enclosure.isEmpty()) return ResponseEntity.notFound().build();
        var enclosureResource = EnclosureResourceFromEntityAssembler.toResourceFromEntity(enclosure.get());
        return ResponseEntity.ok(enclosureResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEnclosure(@PathVariable Long id) {
        var deleteEnclosureCommand = new DeleteEnclosureCommand(id);
        enclosureCommandService.handle(deleteEnclosureCommand);
        return ResponseEntity.ok().body("Enclosure with id " + id + " deleted successfully.");
    }
}
