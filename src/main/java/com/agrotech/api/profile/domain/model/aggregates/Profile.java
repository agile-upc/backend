package com.agrotech.api.profile.domain.model.aggregates;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.profile.domain.model.commands.CreateProfileCommand;
import com.agrotech.api.profile.domain.model.commands.UpdateProfileCommand;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Profile {
    private Long id;
    private String firstName;
    private String lastName;
    private String city;
    private String country;
    private LocalDate birthDate;
    private String description;
    private String photo;
    private String occupation;
    private Integer experience;
    private User user;

    public Profile() {
    }

    public Profile(CreateProfileCommand command, User user, String photoUrl) {
        this.firstName = command.firstName();
        this.lastName = command.lastName();
        this.city = command.city();
        this.country = command.country();
        this.birthDate = command.birthDate();
        this.description = command.description();
        this.photo = photoUrl;
        this.occupation = command.occupation();
        this.experience = command.experience();
        this.user = user;
    }

    public Profile(Long id, String firstName, String lastName, String city, String country,
                   LocalDate birthDate, String description, String photo, String occupation,
                   Integer experience, User user) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.country = country;
        this.birthDate = birthDate;
        this.description = description;
        this.photo = photo;
        this.occupation = occupation;
        this.experience = experience;
        this.user = user;
    }

    public Long getUserId() {
        return this.user.getId();
    }
}
