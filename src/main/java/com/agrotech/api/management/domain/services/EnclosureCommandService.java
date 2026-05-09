package com.agrotech.api.management.domain.services;

import com.agrotech.api.management.domain.model.aggregates.Enclosure;
import com.agrotech.api.management.domain.model.commands.CreateEnclosureCommand;
import com.agrotech.api.management.domain.model.commands.DeleteEnclosureCommand;
import com.agrotech.api.management.domain.model.commands.UpdateEnclosureCommand;

import java.util.Optional;

public interface EnclosureCommandService {
    Long handle(CreateEnclosureCommand command);
    Optional<Enclosure> handle(UpdateEnclosureCommand command);
    void handle(DeleteEnclosureCommand command);
}
