package com.agrotech.api.profile.domain.services;

import com.agrotech.api.profile.domain.model.aggregates.Profile;
import com.agrotech.api.profile.domain.model.commands.CreateProfileCommand;
import com.agrotech.api.profile.domain.model.commands.DeleteProfileCommand;
import com.agrotech.api.profile.domain.model.commands.UpdateProfileCommand;

import java.io.IOException;
import java.util.Optional;

public interface ProfileCommandService {
    Long handle(CreateProfileCommand command) throws IOException;
    Optional<Profile> handle(UpdateProfileCommand command) throws IOException;
    void handle(DeleteProfileCommand command);
}
