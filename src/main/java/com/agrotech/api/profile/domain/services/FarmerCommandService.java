package com.agrotech.api.profile.domain.services;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.profile.domain.model.commands.CreateFarmerCommand;
import com.agrotech.api.profile.domain.model.commands.DeleteFarmerCommand;

public interface FarmerCommandService {
    Long handle(CreateFarmerCommand createFarmerCommand,User user);
    void handle(DeleteFarmerCommand command);
}
