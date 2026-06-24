package com.agrotech.api.profile.infrastructure.persistence.jpa.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface AdvisorRecommendationProjection {
    Long getAdvisorId();
    Long getUserId();
    Long getProfileId();
    BigDecimal getRating();
    String getFirstName();
    String getLastName();
    String getCity();
    String getCountry();
    LocalDate getBirthDate();
    String getDescription();
    String getPhoto();
    String getOccupation();
    String getSpokenLanguages();
    Integer getExperience();
    LocalDate getNextAvailableDate();
}
