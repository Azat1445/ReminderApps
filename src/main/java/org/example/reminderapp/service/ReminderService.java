package org.example.reminderapp.service;

import org.example.reminderapp.dto.request.ReminderCreateDto;
import org.example.reminderapp.dto.request.ReminderFilterDto;
import org.example.reminderapp.dto.request.ReminderUpdateDto;
import org.example.reminderapp.dto.response.ReminderResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reminderapp.entity.Reminder;
import org.example.reminderapp.entity.User;
import org.example.reminderapp.entity.enums.Status;
import org.example.reminderapp.exception.ResourceNotFoundException;
import org.example.reminderapp.mapper.ReminderMapperDto;
import org.example.reminderapp.repository.ReminderRepository;
import org.example.reminderapp.repository.UserRepository;
import org.example.reminderapp.repository.specification.ReminderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;
    private final ReminderMapperDto reminderMapperDto;

    @Transactional(readOnly = true)
    public Page<ReminderResponseDto> findAllReminders(ReminderFilterDto filter,
                                                      Pageable pageable,
                                                      Long currentUserId) {
        log.info("Fetching reminders for user: {}, filter: {}, page: {}", filter, pageable,currentUserId);

        Specification<Reminder> specification = ReminderSpecification.withFilters(filter, currentUserId);
        Page<Reminder> reminders = reminderRepository.findAll(specification, pageable);

        log.info("Found {} reminders for user: {}", reminders.getTotalElements());
        return reminders.map(reminderMapperDto::toDto);
    }

    @Transactional(readOnly = true)
    public ReminderResponseDto findRemindersById(Long id,
                                                 Long currentUserId) {
        log.info("Fetching reminder: {} for user: {}", id, currentUserId);

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));

        checkOwnerShip(reminder,currentUserId);
        return reminderMapperDto.toDto(reminder);
    }

    @Transactional
    public ReminderResponseDto createReminder(ReminderCreateDto dto,
                                              Long currentUserId) {
        log.info("Creating reminder for user: {}, dto: {}", currentUserId, dto);

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        Reminder reminder = reminderMapperDto.toEntity(dto);
        reminder.setUser(user);
        reminder.setStatus(Status.PENDING);

        Reminder saved = reminderRepository.save(reminder);
        log.info("Reminder created with id: {}", saved.getId());

        return reminderMapperDto.toDto(saved);
    }

    @Transactional
    public ReminderResponseDto updateReminder(Long id,
                                              ReminderUpdateDto dto,
                                              Long currentUserId) {
        log.info("Updating reminder: {} for user: {}, dto: {}", id, currentUserId, dto);

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));

        checkOwnerShip(reminder,currentUserId);

        reminderMapperDto.updateEntity(dto, reminder);
        Reminder updated = reminderRepository.save(reminder);
        log.info("Updated reminder successfully: {}", id);

        return reminderMapperDto.toDto(updated);
    }

    @Transactional
    public void deleteReminder(Long id,
                               Long currentUserId) {
        log.info("Deleting reminder: {} for user: {}", id, currentUserId);

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));

        checkOwnerShip(reminder,currentUserId);

        reminderRepository.deleteById(id);
        log.info("Deleted reminder successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public List<ReminderResponseDto> sortReminders(String by, int page, int size, Long currentUserId) {
        log.info("Sorting reminders by: {} for user: {}", by, currentUserId);

        Sort sort = switch (by.toLowerCase()) {
            case "name", "title" -> Sort.by(Sort.Direction.ASC, "title");
            case "date" -> Sort.by(Sort.Direction.ASC, "remindAt");
            case "time" -> Sort.by(Sort.Direction.ASC, "remindAt");
            default -> {
                log.warn("Invalid sort field: {}, using default 'remindAt'", by);
                yield Sort.by(Sort.Direction.ASC, "remindAt");
            }
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Reminder> reminders = reminderRepository.findByUserId(currentUserId, pageable);

        log.info("Found {} reminders after sorting by {}", reminders.getTotalElements(), by);
        return reminders.stream()
                .map(reminderMapperDto::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReminderResponseDto> filterReminders(LocalDate date, LocalTime time, int page, int size, Long currentUserId) {
        log.info("Filtering reminders for user: {}, date: {}, time: {}", currentUserId, date, time);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "remindAt"));
        Page<Reminder> reminders;

        if (date != null && time != null) {
            reminders = reminderRepository.findByUserIdAndRemindAtDateAndTime(currentUserId, date, time, pageable);
        } else if (date != null) {
            reminders = reminderRepository.findByUserIdAndRemindAtDate(currentUserId, date, pageable);
        } else if (time != null) {
            reminders = reminderRepository.findByUserIdAndRemindAtTime(currentUserId, time, pageable);
        } else {
            reminders = reminderRepository.findByUserId(currentUserId, pageable);
        }

        log.info("Filtered {} reminders", reminders.getTotalElements());
        return reminders.stream()
                .map(reminderMapperDto::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ReminderResponseDto> listReminders(int page, int size, Long currentUserId) {
        log.info("Listing reminders for user: {}, page: {}, size: {}", currentUserId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "remindAt"));
        Page<Reminder> reminders = reminderRepository.findByUserId(currentUserId, pageable);

        log.info("Listed {} reminders, total: {}", reminders.getNumberOfElements(), reminders.getTotalElements());
        return reminders.map(reminderMapperDto::toDto);
    }

    @Transactional(readOnly = true)
    public List<ReminderResponseDto> searchReminders(String query, int page, int size, Long currentUserId) {
        log.info("Searching reminders with query: '{}' for user: {}", query, currentUserId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Reminder> reminders = reminderRepository.findByUserIdAndTitleContainingOrDescriptionContaining(
                currentUserId, query, query, pageable
        );

        log.info("Found {} reminders matching query '{}'", reminders.getTotalElements(), query);
        return reminders.stream()
                .map(reminderMapperDto::toDto)
                .toList();
    }

    private void checkOwnerShip(Reminder reminder,
                                Long currentUserId) {
        if (!reminder.getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to access reminder {} owned by user {}", currentUserId, reminder.getId(),
                    reminder.getUser().getId());
            throw new AccessDeniedException("You can only access your own reminders");
        }
    }
}
