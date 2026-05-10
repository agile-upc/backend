package com.agrotech.api.profile.infrastructure.web.controller;

import com.agrotech.api.profile.infrastructure.web.dto.CreateNotificationResource;
import com.agrotech.api.profile.infrastructure.web.dto.NotificationResource;
import com.agrotech.api.profile.application.mapper.NotificationMapper;
import com.agrotech.api.profile.application.usecase.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/notifications", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Notification Management Endpoints")
public class NotificationsController {
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationsController(NotificationService notificationService, NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResource>> getCurrentNotifications() {
        return ResponseEntity.ok(notificationService.getCurrentNotifications().stream().map(notificationMapper::toNotificationResource).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResource> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationMapper.toNotificationResource(notificationService.getNotificationById(id)));
    }

    @PostMapping
    public ResponseEntity<NotificationResource> createNotification(@RequestBody CreateNotificationResource createNotificationResource) {
        return new ResponseEntity<>(notificationMapper.toNotificationResource(notificationService.createNotification(createNotificationResource)), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification with id: " + id + " deleted successfully");
    }
}
