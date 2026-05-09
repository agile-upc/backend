package com.agrotech.api.profile.domain.model.entities;

import com.agrotech.api.iam.domain.model.aggregates.User;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Farmer {
    @Setter
    private Long id;
    private User user;

    public Farmer() {
    }

    public Farmer(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getId();
    }
}