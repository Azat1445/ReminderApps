package org.example.reminderapp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reminderapp.config.CustomUserDetails;
import org.example.reminderapp.dto.request.ReminderCreateDto;
import org.example.reminderapp.dto.request.ReminderFilterDto;
import org.example.reminderapp.dto.response.ReminderResponseDto;
import org.example.reminderapp.dto.request.ReminderUpdateDto;
import org.example.reminderapp.service.ReminderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "Reminders", description = "Reminder management")
@Slf4j
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public ResponseEntity<Page<ReminderResponseDto>> findAllReminders(@ModelAttribute ReminderFilterDto filter,
                                                                      @PageableDefault(size = 20, sort = "remindAt", direction = Sort.Direction.ASC) Pageable pageable,
                                                                      @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = ((CustomUserDetails) userDetails).getId();
        log.info("Fetching reminders with filter: {}, page: {}, userId: {}", filter, pageable, currentUserId);

        Page<ReminderResponseDto> reminders = reminderService.findAllReminders(filter, pageable, currentUserId);
        log.info("Found {} reminders", reminders.getTotalElements());

        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponseDto> findRemindersById(@PathVariable Long id,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = ((CustomUserDetails) userDetails).getId();
        log.info("Getting reminder by id: {} for user: {}", id, currentUserId);

        ReminderResponseDto reminder = reminderService.findRemindersById(id, currentUserId);
        return ResponseEntity.ok(reminder);
    }

    @PostMapping("/reminder/create")
    public ResponseEntity<ReminderResponseDto> createReminder(@Valid @RequestBody ReminderCreateDto createDto,
                                                              @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = ((CustomUserDetails) userDetails).getId();
        log.info("Creating reminder for user: {}, dto: {}", currentUserId, createDto);

        ReminderResponseDto created = reminderService.createReminder(createDto, currentUserId);
        log.info("Created reminder with id: {}", created.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponseDto> updateReminder(@PathVariable Long id,
                                                              @Valid @RequestBody ReminderUpdateDto updateDto,
                                                              @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = ((CustomUserDetails) userDetails).getId();
        log.info("updating reminder: {} by user: {}, dto: {}", id, currentUserId, updateDto);

        ReminderResponseDto updated = reminderService.updateReminder(id, updateDto, currentUserId);
        log.info("Reminder {} updated successfully", id);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = ((CustomUserDetails) userDetails).getId();
        log.info("Deleting reminder: {} for user: {}", id, userId);

        reminderService.deleteReminder(id, userId);
        log.info("Reminder {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/sort")
    public ResponseEntity<List<ReminderResponseDto>> sortReminders(
            @RequestParam String by,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = ((CustomUserDetails) userDetails).getId();
        log.info("Sorting reminders by: {} for user: {}, page: {}, size: {}", by, currentUserId, page, size);

        List<ReminderResponseDto> sorted = reminderService.sortReminders(by, page, size, currentUserId);
        log.info("Sorted {} reminders", sorted.size());

        return ResponseEntity.ok(sorted);
    }

    @GetMapping("/v1/filter")
    public ResponseEntity<List<ReminderResponseDto>> filterReminders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = ((CustomUserDetails) userDetails).getId();
        log.info("Filtering reminders by date: {}, time: {} for user: {}", date, time, currentUserId);

        List<ReminderResponseDto> filtered = reminderService.filterReminders(date, time, page, size, currentUserId);
        log.info("Filtered {} reminders", filtered.size());

        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/v1/list")
    public ResponseEntity<Page<ReminderResponseDto>> listReminders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = ((CustomUserDetails) userDetails).getId();
        log.info("Listing reminders for user: {}, page: {}, size: {}", currentUserId, page, size);

        Page<ReminderResponseDto> reminders = reminderService.listReminders(page, size, currentUserId);
        log.info("Listed {} reminders, total: {}", reminders.getNumberOfElements(), reminders.getTotalElements());

        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/v1/search")
    public ResponseEntity<List<ReminderResponseDto>> searchReminders(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = ((CustomUserDetails) userDetails).getId();
        log.info("Searching reminders with query: '{}' for user: {}", query, currentUserId);

        List<ReminderResponseDto> results = reminderService.searchReminders(query, page, size, currentUserId);
        log.info("Found {} reminders matching query", results.size());

        return ResponseEntity.ok(results);
    }
}

