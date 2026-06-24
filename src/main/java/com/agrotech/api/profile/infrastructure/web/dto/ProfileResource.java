package com.agrotech.api.profile.infrastructure.web.dto;

import java.time.LocalDate;

public record ProfileResource(Long id,
                              Long userId,
                              String firstName,
                              String lastName,
                              String city,
                              String country,
                              LocalDate birthDate,
                              String description,
                              String photo,
                              String occupation,
                              String spokenLanguages,
                              Integer experience){}
