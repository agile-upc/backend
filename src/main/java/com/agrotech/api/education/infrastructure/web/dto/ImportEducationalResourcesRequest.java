package com.agrotech.api.education.infrastructure.web.dto;

import java.util.List;

public record ImportEducationalResourcesRequest(List<ImportEducationalResourceResource> resources) {
}
