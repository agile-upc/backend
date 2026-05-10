package com.agrotech.api.profile.application.usecase;

import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.model.User;
import com.agrotech.api.iam.infrastructure.persistence.jpa.repository.UserRepository;
import com.agrotech.api.profile.infrastructure.web.dto.CreateNotificationResource;
import com.agrotech.api.profile.application.mapper.NotificationMapper;
import com.agrotech.api.profile.domain.model.Notification;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final NotificationMapper notificationMapper;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            AuthenticatedUserService authenticatedUserService,
            NotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.notificationMapper = notificationMapper;
    }

    private List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUser_Id(userId).stream()
                .sorted(Comparator.comparing(Notification::getSendAt).reversed())
                .toList();
    }

    public List<Notification> getCurrentNotifications() {
        return getNotificationsByUserId(authenticatedUserService.getCurrentUser().userId());
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    }

    @Transactional
    public Notification createNotification(CreateNotificationResource resource) {
        return createNotification(resource.userId(), resource.title(), resource.message(), resource.sendAt());
    }

    @Transactional
    public Notification createNotification(Long userId, String title, String message, Date sendAt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Notification notification = notificationMapper.toNotification(user, title, message, sendAt);
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notificationRepository.delete(notification);
    }
}
