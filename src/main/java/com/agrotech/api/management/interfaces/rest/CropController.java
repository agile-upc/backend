package com.agrotech.api.management.interfaces.rest;

import com.agrotech.api.management.domain.model.aggregates.Crop;
import com.agrotech.api.management.domain.model.commands.DeleteCropCommand;
import com.agrotech.api.management.domain.model.queries.GetAllCropsByFarmerIdQuery;
import com.agrotech.api.management.domain.model.queries.GetAllCropsQuery;
import com.agrotech.api.management.domain.model.queries.GetCropByIdQuery;
import com.agrotech.api.management.domain.services.CropCommandService;
import com.agrotech.api.management.domain.services.CropQueryService;
import com.agrotech.api.management.interfaces.rest.resources.*;
import com.agrotech.api.management.interfaces.rest.transform.CreateCropCommandFromResourceAssembler;
import com.agrotech.api.management.interfaces.rest.transform.CropResourceFromEntityAssembler;
import com.agrotech.api.management.interfaces.rest.transform.CropThresholdsResourceFromEntityAssembler;
import com.agrotech.api.management.interfaces.rest.transform.UpdateCropCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/crops", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Crops", description = "Crop Management Endpoints")
public class CropController {
    private final CropCommandService cropCommandService;
    private final CropQueryService cropQueryService;

    public CropController(CropCommandService cropCommandService, CropQueryService cropQueryService){
        this.cropCommandService = cropCommandService;
        this.cropQueryService = cropQueryService;
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CropResource>> getCrops(
            @RequestParam(value = "farmerId", required = false) Long farmerId
    ) {
        List<Crop> crops;
        if (farmerId != null) {
            var getAllCropsByFarmerIdQuery = new GetAllCropsByFarmerIdQuery(farmerId);
            crops = cropQueryService.handle(getAllCropsByFarmerIdQuery);
        } else {
            var getAllCropsQuery = new GetAllCropsQuery();
            crops = cropQueryService.handle(getAllCropsQuery);
        }
        var cropResources = crops.stream().map(CropResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(cropResources);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CropResource> getCropById(@PathVariable Long id) {
        var getCropByIdQuery = new GetCropByIdQuery(id);
        var crop = cropQueryService.handle(getCropByIdQuery);
        if (crop.isEmpty()) return ResponseEntity.notFound().build();
        var cropResource = CropResourceFromEntityAssembler.toResourceFromEntity(crop.get());
        return ResponseEntity.ok(cropResource);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CropResource> createCrop(@RequestBody CreateCropResource resource) {
        var createCropCommand = CreateCropCommandFromResourceAssembler.toCommandFromResource(resource);
        Long cropId = cropCommandService.handle(createCropCommand);
        var crop = cropQueryService.handle(new GetCropByIdQuery(cropId));
        if (crop.isEmpty()) return ResponseEntity.badRequest().build();
        var cropResource = CropResourceFromEntityAssembler.toResourceFromEntity(crop.get());
        return new ResponseEntity<>(cropResource, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CropResource> updateCrop(@PathVariable Long id, @RequestBody UpdateCropResource resource) {
        var updateCropCommand = UpdateCropCommandFromResourceAssembler.toCommandFromResource(id, resource);
        Optional<Crop> crop = cropCommandService.handle(updateCropCommand);
        if (crop.isEmpty()) return ResponseEntity.notFound().build();
        var cropResource = CropResourceFromEntityAssembler.toResourceFromEntity(crop.get());
        return ResponseEntity.ok(cropResource);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCrop(@PathVariable Long id) {
        var deleteCropCommand = new DeleteCropCommand(id);
        cropCommandService.handle(deleteCropCommand);
        return ResponseEntity.ok().body("Crop with id " + id + " deleted successfully.");
    }

    @PutMapping("/{id}/iot")
    public ResponseEntity<?> updateIot(@PathVariable Long id, @RequestBody UpdateIotCropResource resource) {
        var updateIotCropCommand = UpdateCropCommandFromResourceAssembler.toIotCommandFromResource(id, resource);
        Optional<Crop> crop = cropCommandService.handle(updateIotCropCommand);
        if (crop.isEmpty()) return ResponseEntity.notFound().build();
        var cropResource = CropResourceFromEntityAssembler.toResourceFromEntity(crop.get());
        return ResponseEntity.ok().body(cropResource);
    }

    @GetMapping("/{id}/thresholds")
    public ResponseEntity<CropThresholdsResource> getCropThresholds(@PathVariable Long id) {
        var getCropByIdQuery = new GetCropByIdQuery(id);
        var crop = cropQueryService.handle(getCropByIdQuery);
        if (crop.isEmpty()) return ResponseEntity.notFound().build();
        var cropResource = CropThresholdsResourceFromEntityAssembler.toResourceFromEntity(crop.get());
        return ResponseEntity.ok(cropResource);
    }
}
