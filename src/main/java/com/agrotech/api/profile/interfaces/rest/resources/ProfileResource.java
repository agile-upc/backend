package com.agrotech.api.profile.interfaces.rest.resources;

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
                              Integer experience){}
