package com.agrotech.api.profile.application.internal.commandservices;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.FarmerMapper;
import com.agrotech.api.shared.domain.exceptions.FarmerNotFoundException;
import com.agrotech.api.shared.domain.exceptions.UserNotFoundException;
import com.agrotech.api.profile.domain.model.commands.CreateFarmerCommand;
import com.agrotech.api.profile.domain.model.commands.DeleteFarmerCommand;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import com.agrotech.api.profile.domain.services.FarmerCommandService;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repositories.FarmerRepository;
import org.springframework.stereotype.Service;

@Service
public class FarmerCommandServiceImpl implements FarmerCommandService {
    private final FarmerRepository farmerRepository;

    public FarmerCommandServiceImpl(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    @Override
    public Long handle(CreateFarmerCommand command, User user) {
        var sameUser = farmerRepository.findByUser_Id(command.userId());
        if (sameUser.isPresent()) throw new UserNotFoundException(command.userId());
        var farmer = new Farmer(user);
        var farmerEntity = farmerRepository.save(FarmerMapper.toEntity(farmer));
        return farmerEntity.getId();
    }

    @Override
    public void handle(DeleteFarmerCommand command) {
        var farmerEntity = farmerRepository.findById(command.id())
                .orElseThrow(() -> new FarmerNotFoundException(command.id()));
        farmerRepository.delete(farmerEntity);
    }
}
