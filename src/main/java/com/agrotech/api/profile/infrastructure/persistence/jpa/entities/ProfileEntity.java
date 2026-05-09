package com.agrotech.api.profile.infrastructure.persistence.jpa.entities;

import com.agrotech.api.iam.infrastructure.persistence.jpa.entities.UserEntity;
import com.agrotech.api.profile.domain.model.commands.UpdateProfileCommand;
import com.agrotech.api.shared.infrastructure.persistence.jpa.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String city;
    private String country;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate birthDate;

    private String description;
    private String photo;
    private String occupation;

    @Builder.Default
    private Integer experience = 0;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public void update(UpdateProfileCommand command, String photoUrl) {
        this.firstName = command.firstName();
        this.lastName = command.lastName();
        this.city = command.city();
        this.country = command.country();
        this.birthDate = command.birthDate();
        this.description = command.description();
        if (!Objects.equals(photoUrl, "null")) this.photo = photoUrl;
        this.occupation = command.occupation();
        this.experience = command.experience();
    }
}