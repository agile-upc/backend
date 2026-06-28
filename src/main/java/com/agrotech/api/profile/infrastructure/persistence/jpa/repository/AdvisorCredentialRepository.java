package com.agrotech.api.profile.infrastructure.persistence.jpa.repository;

import com.agrotech.api.profile.domain.model.AdvisorCredential;
import com.agrotech.api.profile.domain.valueobject.AdvisorCredentialStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvisorCredentialRepository extends JpaRepository<AdvisorCredential, Long> {
    boolean existsByAdvisor_IdAndStatus(Long advisorId, AdvisorCredentialStatus status);

    List<AdvisorCredential> findByAdvisor_IdOrderByCreatedAtDesc(Long advisorId);

    List<AdvisorCredential> findAllByOrderByCreatedAtDesc();
}
