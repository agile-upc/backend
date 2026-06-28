package com.agrotech.api.education.infrastructure.web.controller;

import com.agrotech.api.education.application.mapper.EducationalResourceMapper;
import com.agrotech.api.education.application.usecase.EducationalResourceService;
import com.agrotech.api.education.infrastructure.web.dto.EducationalResourceResource;
import com.agrotech.api.education.infrastructure.web.dto.ImportEducationalResourceResource;
import com.agrotech.api.education.infrastructure.web.dto.ImportEducationalResourcesRequest;
import com.agrotech.api.education.infrastructure.web.dto.ImportEducationalResourcesResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/educational-resources", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Educational Resources", description = "Educational Resource Endpoints")
public class EducationalResourcesController {
    private final EducationalResourceService educationalResourceService;
    private final EducationalResourceMapper educationalResourceMapper;

    public EducationalResourcesController(
            EducationalResourceService educationalResourceService,
            EducationalResourceMapper educationalResourceMapper
    ) {
        this.educationalResourceService = educationalResourceService;
        this.educationalResourceMapper = educationalResourceMapper;
    }

    @GetMapping
    public ResponseEntity<List<EducationalResourceResource>> getResources() {
        return ResponseEntity.ok(educationalResourceService.getResources().stream()
                .map(educationalResourceMapper::toResource)
                .toList());
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<EducationalResourceResource> createResource(
            @RequestBody ImportEducationalResourceResource resource
    ) {
        return new ResponseEntity<>(
                educationalResourceMapper.toResource(educationalResourceService.createResource(resource)),
                HttpStatus.CREATED
        );
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<EducationalResourceResource> updateResource(
            @PathVariable Long id,
            @RequestBody ImportEducationalResourceResource resource
    ) {
        return ResponseEntity.ok(educationalResourceMapper.toResource(educationalResourceService.updateResource(id, resource)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        educationalResourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportEducationalResourcesResponse> importResources(
            @RequestBody ImportEducationalResourcesRequest request
    ) {
        return ResponseEntity.ok(educationalResourceService.importResources(request));
    }
}
