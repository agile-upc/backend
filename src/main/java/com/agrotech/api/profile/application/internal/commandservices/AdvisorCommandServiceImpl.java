package com.agrotech.api.profile.application.internal.commandservices;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.AdvisorMapper;
import com.agrotech.api.shared.domain.exceptions.AdvisorNotFoundException;
import com.agrotech.api.profile.domain.exceptions.UserAlreadyUsedException;
import com.agrotech.api.profile.domain.model.commands.CreateAdvisorCommand;
import com.agrotech.api.profile.domain.model.commands.DeleteAdvisorCommand;
import com.agrotech.api.profile.domain.model.commands.UpdateAdvisorCommand;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import com.agrotech.api.profile.domain.services.AdvisorCommandService;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repositories.AdvisorRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdvisorCommandServiceImpl implements AdvisorCommandService {
    private final AdvisorRepository advisorRepository;

    public AdvisorCommandServiceImpl(AdvisorRepository advisorRepository) {
        this.advisorRepository = advisorRepository;
    }

    @Override
    public Long handle(CreateAdvisorCommand command, User user) {
        advisorRepository.findByUser_Id(command.userId())
                .ifPresent(existingAdvisor -> {
                    throw new UserAlreadyUsedException(command.userId());
                });
        var advisor = new Advisor(user);
        var advisorEntity = advisorRepository.save(AdvisorMapper.toEntity(advisor));
        return advisorEntity.getId();
    }

    @Override
    public Optional<Advisor> handle(UpdateAdvisorCommand command) {
        var advisorEntity = advisorRepository.findById(command.id())
                .orElseThrow(() -> new AdvisorNotFoundException(command.id()));
        var advisor = AdvisorMapper.toDomain(advisorEntity).update(command);
        var updatedEntity = advisorRepository.save(AdvisorMapper.toEntity(advisor));
        return Optional.of(AdvisorMapper.toDomain(updatedEntity));
    }

    @Override
    public void handle(DeleteAdvisorCommand command) {
        var advisorEntity = advisorRepository.findById(command.id())
                .orElseThrow(() -> new AdvisorNotFoundException(command.id()));
        advisorRepository.delete(advisorEntity);
    }
}
