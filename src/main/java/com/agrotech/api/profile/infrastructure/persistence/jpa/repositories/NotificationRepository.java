package com.agrotech.api.profile.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUser_Id(Long userId);
}
