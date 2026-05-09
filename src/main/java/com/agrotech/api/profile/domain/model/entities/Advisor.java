package com.agrotech.api.profile.domain.model.entities;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.profile.domain.model.commands.UpdateAdvisorCommand;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
public class Advisor {
    @Setter
    private Long id;
    private User user;
    private BigDecimal rating;

    public Advisor() {
    }

    public Advisor(User user) {
        this.user = user;
        this.rating = BigDecimal.valueOf(0.00);
    }

    public Advisor(User user, BigDecimal rating) {
        this.user = user;
        this.rating = rating;
    }

    public Advisor update(UpdateAdvisorCommand command) {
        this.rating = command.rating();
        return this;
    }

    public Long getUserId() {
        return user.getId();
    }
}