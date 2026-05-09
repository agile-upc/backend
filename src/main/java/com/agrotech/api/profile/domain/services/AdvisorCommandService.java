package com.agrotech.api.profile.domain.services;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.profile.domain.model.commands.CreateAdvisorCommand;
import com.agrotech.api.profile.domain.model.commands.DeleteAdvisorCommand;
import com.agrotech.api.profile.domain.model.commands.UpdateAdvisorCommand;
import com.agrotech.api.profile.domain.model.entities.Advisor;

import java.util.Optional;

public interface AdvisorCommandService {
    Long handle(CreateAdvisorCommand command, User user);
    Optional<Advisor> handle(UpdateAdvisorCommand command);
    void handle(DeleteAdvisorCommand command);
}
