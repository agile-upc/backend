package com.agrotech.api.management.interfaces.rest;

import com.agrotech.api.management.domain.model.commands.DeleteAnimalCommand;
import com.agrotech.api.management.domain.model.entities.Animal;
import com.agrotech.api.management.domain.model.queries.GetAllAnimalsByEnclosureIdQuery;
import com.agrotech.api.management.domain.model.queries.GetAllAnimalsQuery;
import com.agrotech.api.management.domain.model.queries.GetAnimalByIdQuery;
import com.agrotech.api.management.domain.services.AnimalCommandService;
import com.agrotech.api.management.domain.services.AnimalQueryService;
import com.agrotech.api.management.interfaces.rest.resources.AnimalResource;
import com.agrotech.api.management.interfaces.rest.resources.CreateAnimalResource;
import com.agrotech.api.management.interfaces.rest.resources.UpdateAnimalResource;
import com.agrotech.api.management.interfaces.rest.transform.AnimalResourceFromEntityAssembler;
import com.agrotech.api.management.interfaces.rest.transform.CreateAnimalCommandFromResourceAssembler;
import com.agrotech.api.management.interfaces.rest.transform.UpdateAnimalCommandFromResourceAssembler;
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
@RequestMapping(value = "/api/v1/animals", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Animals", description = "Animal Management Endpoints")
public class AnimalsController {
    private final AnimalCommandService animalCommandService;
    private final AnimalQueryService animalQueryService;


    public AnimalsController(AnimalCommandService animalCommandService, AnimalQueryService animalQueryService) {
        this.animalCommandService = animalCommandService;
        this.animalQueryService = animalQueryService;
    }

    @GetMapping
    public ResponseEntity<List<AnimalResource>> getAnimals(
            @RequestParam(value="enclosureId", required = false) Long enclosureId
    ) {
        List<Animal> animals;
        if (enclosureId != null) {
            var getAllAnimalsByEnclosureIdQuery = new GetAllAnimalsByEnclosureIdQuery(enclosureId);
            animals = animalQueryService.handle(getAllAnimalsByEnclosureIdQuery);
        }
        else
        {
            var getAllAnimalsQuery = new GetAllAnimalsQuery();
            animals = animalQueryService.handle(getAllAnimalsQuery);
        }
        var animalResources = animals.stream().map(AnimalResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(animalResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResource> getAnimalById(@PathVariable Long id) {
        var getAnimalByIdQuery = new GetAnimalByIdQuery(id);
        var animal = animalQueryService.handle(getAnimalByIdQuery);
        if (animal.isEmpty()) return ResponseEntity.notFound().build();
        var animalResource = AnimalResourceFromEntityAssembler.toResourceFromEntity(animal.get());
        return ResponseEntity.ok(animalResource);
    }

    @PostMapping
    public ResponseEntity<AnimalResource> createAnimal(@RequestBody CreateAnimalResource createAnimalResource) {
        var createAnimalCommand = CreateAnimalCommandFromResourceAssembler.toCommandFromResource(createAnimalResource);
        Long animalId = animalCommandService.handle(createAnimalCommand);
        var animal = animalQueryService.handle(new GetAnimalByIdQuery(animalId));
        if (animal.isEmpty()) return ResponseEntity.badRequest().build();
        var animalResource = AnimalResourceFromEntityAssembler.toResourceFromEntity(animal.get());
        return new ResponseEntity<>(animalResource, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResource> updateAnimal(@PathVariable Long id, @RequestBody UpdateAnimalResource updateAnimalResource) {
        var updateAnimalCommand = UpdateAnimalCommandFromResourceAssembler.toCommandFromResource(id, updateAnimalResource);
        Optional<Animal> animal = animalCommandService.handle(updateAnimalCommand);
        if (animal.isEmpty()) return ResponseEntity.notFound().build();
        var animalResource = AnimalResourceFromEntityAssembler.toResourceFromEntity(animal.get());
        return ResponseEntity.ok(animalResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnimal(@PathVariable Long id) {
        var deleteAnimalCommand = new DeleteAnimalCommand(id);
        animalCommandService.handle(deleteAnimalCommand);
        return ResponseEntity.ok().body("Animal with id " + id + " deleted successfully.");
    }
}
