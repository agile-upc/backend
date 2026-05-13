package com.agrotech.api.appointment.application.usecase;

import com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus;
import com.agrotech.api.appointment.domain.model.AvailableDate;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repository.AvailableDateRepository;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.application.usecase.ProfileService;
import com.agrotech.api.appointment.infrastructure.web.dto.CreateAvailableDateResource;
import com.agrotech.api.appointment.infrastructure.web.dto.UpdateAvailableDateResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AvailableDateService {
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?\\d|2[0-3]):[0-5]\\d$");

    private final AvailableDateRepository availableDateRepository;
    private final ProfileService profileService;

    public AvailableDateService(AvailableDateRepository availableDateRepository, ProfileService profileService) {
        this.availableDateRepository = availableDateRepository;
        this.profileService = profileService;
    }

    public List<AvailableDate> getAvailableDates(Long advisorId, Boolean isAvailable) {
        List<AvailableDate> availableDates;

        if (advisorId != null && isAvailable != null) {
            availableDates = availableDateRepository.findByAdvisor_IdAndStatusOrderByScheduledDateAscStartTimeAsc(
                    advisorId,
                    toStatus(isAvailable)
            );
        } else if (advisorId != null) {
            availableDates = availableDateRepository.findByAdvisor_IdOrderByScheduledDateAscStartTimeAsc(advisorId);
        } else if (isAvailable != null) {
            availableDates = availableDateRepository.findByStatusOrderByScheduledDateAscStartTimeAsc(toStatus(isAvailable));
        } else {
            availableDates = availableDateRepository.findAllByOrderByScheduledDateAscStartTimeAsc();
        }

        return availableDates.stream()
                .filter(this::cleanupPastAvailability)
                .toList();
    }

    public AvailableDate getAvailableDateById(Long id) {
        AvailableDate availableDate = requireAvailableDate(id);
        cleanupPastAvailability(availableDate);
        return availableDate;
    }

    @Transactional
    public AvailableDate createAvailableDate(CreateAvailableDateResource resource) {
        Advisor advisor = profileService.requireCurrentAdvisorEntity();
        validateSchedule(resource.scheduledDate(), resource.startTime(), resource.endTime());

        availableDateRepository.findByAdvisor_IdAndScheduledDateAndStartTimeAndEndTime(
                advisor.getId(),
                resource.scheduledDate(),
                resource.startTime(),
                resource.endTime()
        ).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An available date already exists for that slot");
        });

        AvailableDate availableDate = availableDateRepository.save(AvailableDate.builder()
                .advisor(advisor)
                .scheduledDate(resource.scheduledDate())
                .startTime(resource.startTime())
                .endTime(resource.endTime())
                .status(AvailableDateStatus.AVAILABLE)
                .build());

        return availableDate;
    }

    @Transactional
    public AvailableDate updateAvailableDate(Long id, UpdateAvailableDateResource resource) {
        AvailableDate availableDate = requireAvailableDate(id);
        validateSchedule(resource.scheduledDate(), resource.startTime(), resource.endTime());
        availableDate.setScheduledDate(resource.scheduledDate());
        availableDate.setStartTime(resource.startTime());
        availableDate.setEndTime(resource.endTime());
        return availableDateRepository.save(availableDate);
    }

    public void deleteAvailableDate(Long id) {
        availableDateRepository.delete(requireAvailableDate(id));
    }

    public AvailableDate requireAvailableDate(Long id) {
        return availableDateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Available date not found"));
    }

    private AvailableDateStatus toStatus(Boolean isAvailable) {
        return isAvailable ? AvailableDateStatus.AVAILABLE : AvailableDateStatus.UNAVAILABLE;
    }

    private boolean cleanupPastAvailability(AvailableDate availableDate) {
        if (availableDate.getScheduledDate().isBefore(LocalDate.now()) && availableDate.getStatus() == AvailableDateStatus.AVAILABLE) {
            availableDateRepository.delete(availableDate);
            return false;
        }
        return true;
    }

    private void validateSchedule(LocalDate scheduledDate, String startTime, String endTime) {
        if (scheduledDate == null || scheduledDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scheduled date must be today or later");
        }
        if (startTime == null || endTime == null || !TIME_PATTERN.matcher(startTime).matches() || !TIME_PATTERN.matcher(endTime).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Time must use HH:mm format");
        }
        if (startTime.compareTo(endTime) >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }
    }

}
