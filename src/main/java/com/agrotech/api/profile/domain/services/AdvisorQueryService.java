package com.agrotech.api.profile.domain.services;

import com.agrotech.api.profile.domain.model.entities.Advisor;
import com.agrotech.api.profile.domain.model.queries.GetAdvisorByIdQuery;
import com.agrotech.api.profile.domain.model.queries.GetAdvisorByUserIdQuery;
import com.agrotech.api.profile.domain.model.queries.GetAllAdvisorsQuery;

import java.util.List;
import java.util.Optional;

public interface AdvisorQueryService {
    Optional<Advisor> handle(GetAdvisorByIdQuery query);
    List<Advisor> handle(GetAllAdvisorsQuery query);
    Optional<Advisor> handle(GetAdvisorByUserIdQuery query);
}
