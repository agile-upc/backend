package com.agrotech.api.profile.interfaces.rest.transform;

import com.agrotech.api.profile.domain.model.entities.Farmer;
import com.agrotech.api.profile.interfaces.rest.resources.FarmerResource;

public class FarmerResourceFromEntityAssembler {
    public static FarmerResource toResourceFromEntity(Farmer entity) {
        return new FarmerResource(
                entity.getId(),
                entity.getUserId());
    }
}
