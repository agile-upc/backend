package com.agrotech.api.profile.infrastructure.persistence.jpa.repository;

import com.agrotech.api.profile.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_Id(Long userId);
}
