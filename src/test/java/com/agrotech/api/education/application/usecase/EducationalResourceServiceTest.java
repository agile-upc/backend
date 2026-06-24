package com.agrotech.api.education.application.usecase;

import com.agrotech.api.education.application.mapper.EducationalResourceMapper;
import com.agrotech.api.education.domain.model.EducationalResource;
import com.agrotech.api.education.domain.valueobject.EducationalResourceType;
import com.agrotech.api.education.infrastructure.persistence.jpa.repository.EducationalResourceRepository;
import com.agrotech.api.education.infrastructure.web.dto.ImportEducationalResourceResource;
import com.agrotech.api.education.infrastructure.web.dto.ImportEducationalResourcesRequest;
import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EducationalResourceServiceTest {
    private final EducationalResourceRepository repository = mock(EducationalResourceRepository.class);
    private final AuthenticatedUserService authenticatedUserService = mock(AuthenticatedUserService.class);
    private final EducationalResourceService service = new EducationalResourceService(
            repository,
            new EducationalResourceMapper(),
            authenticatedUserService
    );

    @Test
    void importResourcesUpsertsBySourceUrlAndSkipsInvalidRows() {
        EducationalResource existing = EducationalResource.builder()
                .title("Old title")
                .type(EducationalResourceType.GUIDE)
                .sourceName("SENASA")
                .sourceUrl("https://example.com/guide")
                .topics(List.of("Old"))
                .build();

        when(repository.findBySourceUrl("https://example.com/guide")).thenReturn(Optional.of(existing));
        when(repository.findBySourceUrl("https://example.com/manual")).thenReturn(Optional.empty());
        when(repository.save(any(EducationalResource.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.importResources(new ImportEducationalResourcesRequest(List.of(
                new ImportEducationalResourceResource(
                        "Updated guide",
                        "Useful guide",
                        EducationalResourceType.GUIDE,
                        "SENASA",
                        "https://example.com/guide",
                        null,
                        null,
                        null,
                        List.of("Buenas practicas")
                ),
                new ImportEducationalResourceResource(
                        "New manual",
                        "Useful manual",
                        EducationalResourceType.MANUAL,
                        "INIA",
                        "https://example.com/manual",
                        "https://example.com/manual.pdf",
                        null,
                        null,
                        List.of("Cultivos")
                ),
                new ImportEducationalResourceResource(
                        "",
                        "Invalid row",
                        EducationalResourceType.BOOK,
                        "MIDAGRI",
                        "https://example.com/invalid",
                        null,
                        null,
                        null,
                        List.of()
                )
        )));

        verify(authenticatedUserService).requireRole(UserRole.ADMIN);
        assertEquals(1, response.created());
        assertEquals(1, response.updated());
        assertEquals(1, response.skipped());
        assertEquals("Updated guide", existing.getTitle());
        assertEquals(List.of("Buenas practicas"), existing.getTopics());
    }
}
