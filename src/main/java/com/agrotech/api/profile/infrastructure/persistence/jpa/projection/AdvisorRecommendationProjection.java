package com.agrotech.api.profile.infrastructure.persistence.jpa.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface AdvisorRecommendationProjection {
    Long getAdvisorId();
    Long getUserId();
    BigDecimal getRating();
    String getFirstName();
    String getLastName();
    String getCity();
    String getCountry();
    String getDescription();
    String getPhoto();
    String getOccupation();
    Integer getExperience();
    LocalDate getNextAvailableDate();
}
