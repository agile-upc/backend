package com.agrotech.api.profile.interfaces.acl;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.profile.domain.model.aggregates.Profile;
import com.agrotech.api.profile.domain.model.commands.CreateAdvisorCommand;
import com.agrotech.api.profile.domain.model.commands.CreateFarmerCommand;
import com.agrotech.api.profile.domain.model.commands.UpdateAdvisorCommand;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import com.agrotech.api.profile.domain.model.queries.*;
import com.agrotech.api.profile.domain.services.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProfilesContextFacade {
    private final ProfileQueryService profileQueryService;
    private final FarmerQueryService farmerQueryService;
    private final FarmerCommandService farmerCommandService;
    private final AdvisorQueryService advisorQueryService;
    private final AdvisorCommandService advisorCommandService;


    public ProfilesContextFacade(ProfileQueryService profileQueryService, FarmerQueryService farmerQueryService,
                                 FarmerCommandService farmerCommandService, AdvisorQueryService advisorQueryService,
                                 AdvisorCommandService advisorCommandService) {
        this.profileQueryService = profileQueryService;
        this.farmerQueryService = farmerQueryService;
        this.advisorQueryService = advisorQueryService;
        this.advisorCommandService = advisorCommandService;
        this.farmerCommandService = farmerCommandService;
    }

    public Optional<Profile> fetchProfileByFarmerId(Long farmerId) {
        var farmerProfileQuery = new GetFarmerByIdQuery(farmerId);
        var farmer = farmerQueryService.handle(farmerProfileQuery);
        if (farmer.isEmpty()) return Optional.empty();
        Long userId = farmer.get().getUserId();
        var profileQuery = new GetProfileByUserIdQuery(userId);
        return profileQueryService.handle(profileQuery);
    }

    public Optional<Profile> fetchProfileByAdvisorId(Long advisorId) {
        var advisorProfileQuery = new GetAdvisorByIdQuery(advisorId);
        var advisor = advisorQueryService.handle(advisorProfileQuery);
        if (advisor.isEmpty()) return Optional.empty();
        Long userId = advisor.get().getUserId();
        var profileQuery = new GetProfileByUserIdQuery(userId);
        return profileQueryService.handle(profileQuery);
    }

    public Optional<Farmer> fetchFarmerById(Long farmerId) {
        var getFarmerByIdQuery = new GetFarmerByIdQuery(farmerId);
        return farmerQueryService.handle(getFarmerByIdQuery);
    }

    public Optional<Advisor> fetchAdvisorById(Long advisorId) {
        var getAdvisorByIdQuery = new GetAdvisorByIdQuery(advisorId);
        return advisorQueryService.handle(getAdvisorByIdQuery);
    }

    public void updateRating(Long advisorId, BigDecimal rating) {
        var advisor = advisorQueryService.handle(new GetAdvisorByIdQuery(advisorId));
        if (advisor.isEmpty()) return;
        advisorCommandService.handle(new UpdateAdvisorCommand(advisor.get().getId(), rating));
    }

    public Long createFarmer(Long userId, User user) {
        return farmerCommandService.handle(new CreateFarmerCommand(userId), user);
    }

    public Long createAdvisor(Long userId, User user) {
        return advisorCommandService.handle(new CreateAdvisorCommand(userId), user);
    }
}
