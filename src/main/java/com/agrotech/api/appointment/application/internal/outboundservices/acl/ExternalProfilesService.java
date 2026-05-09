package com.agrotech.api.appointment.application.internal.outboundservices.acl;

import com.agrotech.api.profile.domain.model.aggregates.Profile;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import com.agrotech.api.profile.interfaces.acl.ProfilesContextFacade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ExternalProfilesService {
    private final ProfilesContextFacade profilesContextFacade;

    public ExternalProfilesService(ProfilesContextFacade profilesContextFacade) {
        this.profilesContextFacade = profilesContextFacade;
    }

    public Optional<Advisor> fetchAdvisorById(Long advisorId) {
        return profilesContextFacade.fetchAdvisorById(advisorId);
    }

    public Optional<Farmer> fetchFarmerById(Long farmerId) {
        return profilesContextFacade.fetchFarmerById(farmerId);
    }

    public Optional<Profile> fetchProfileByFarmerId(Long farmerId) {
        return profilesContextFacade.fetchProfileByFarmerId(farmerId);
    }

    public Optional<Profile> fetchProfileByAdvisorId(Long advisorId) {
        return profilesContextFacade.fetchProfileByAdvisorId(advisorId);
    }

    public void updateRating(Long advisorId, BigDecimal rating) {
        profilesContextFacade.updateRating(advisorId, rating);
    }


}
