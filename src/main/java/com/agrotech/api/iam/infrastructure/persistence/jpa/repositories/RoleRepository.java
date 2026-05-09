package com.agrotech.api.iam.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.iam.domain.model.valueobjects.Roles;
import com.agrotech.api.iam.infrastructure.persistence.jpa.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(Roles name);
    boolean existsByName(Roles name);

}