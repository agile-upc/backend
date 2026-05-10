package com.agrotech.api.profile.infrastructure.persistence.jpa.repository;

import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.domain.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser_Id(Long userId);
    List<Profile> findByUser_Role(UserRole role);
}
