package org.example.reminderapp.service;

import org.example.reminderapp.dto.request.ReminderCreateDto;
import org.example.reminderapp.dto.response.ReminderResponseDto;
import org.example.reminderapp.dto.request.ReminderUpdateDto;
import org.example.reminderapp.entity.Reminder;
import org.example.reminderapp.entity.User;
import org.example.reminderapp.entity.enums.ReminderType;
import org.example.reminderapp.entity.enums.Status;
import org.example.reminderapp.exception.ResourceNotFoundException;
import org.example.reminderapp.mapper.ReminderMapperDto;
import org.example.reminderapp.repository.ReminderRepository;
import org.example.reminderapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReminderMapperDto reminderMapperDto;

    @InjectMocks
    private ReminderService reminderService;

    private User testUser;
    private Reminder testReminder;
    private ReminderCreateDto createDto;
    private ReminderResponseDto responseDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testReminder = new Reminder();
        testReminder.setId(1L);
        testReminder.setTitle("Test Reminder");
        testReminder.setDescription("Test Description");
        testReminder.setUser(testUser);
        testReminder.setRemindAt(OffsetDateTime.now().plusDays(1));
        testReminder.setType(ReminderType.EMAIL);
        testReminder.setStatus(Status.PENDING);

        createDto = new ReminderCreateDto();
        createDto.setTitle("New Reminder");
        createDto.setDescription("New Description");
        createDto.setRemindAt(OffsetDateTime.now().plusDays(1));
        createDto.setType(ReminderType.EMAIL);

        responseDto = new ReminderResponseDto();
        responseDto.setId(1L);
        responseDto.setTitle("Test Reminder");
    }

    @Test
    void findReminderByIdSuccess() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));
        when(reminderMapperDto.toDto(testReminder)).thenReturn(responseDto);

        ReminderResponseDto result = reminderService.findRemindersById(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(reminderRepository).findById(1L);
        verify(reminderMapperDto).toDto(testReminder);
    }

    @Test
    void findReminderByIdNotFound() {
        when(reminderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reminderService.findRemindersById(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reminder not found with id: 999");
    }

    @Test
    void findReminderByIdAccessDenied() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));

        assertThatThrownBy(() -> reminderService.findRemindersById(1L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only access your own reminders");
    }

    @Test
    void createReminderSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reminderMapperDto.toEntity(createDto)).thenReturn(testReminder);
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);
        when(reminderMapperDto.toDto(testReminder)).thenReturn(responseDto);

        ReminderResponseDto result = reminderService.createReminder(createDto, 1L);

        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(reminderRepository).save(any(Reminder.class));
    }

    @Test
    void createReminderUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reminderService.createReminder(createDto, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    void updateReminderSuccess() {
        ReminderUpdateDto updateDto = new ReminderUpdateDto();
        updateDto.setTitle("Updated Title");

        when(reminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);
        when(reminderMapperDto.toDto(testReminder)).thenReturn(responseDto);

        ReminderResponseDto result = reminderService.updateReminder(1L, updateDto, 1L);

        assertThat(result).isNotNull();
        verify(reminderMapperDto).updateEntity(updateDto, testReminder);
        verify(reminderRepository).save(testReminder);
    }

    @Test
    void deleteReminderSuccess() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));

        reminderService.deleteReminder(1L, 1L);

        verify(reminderRepository).deleteById(1L);
    }

    @Test
    void deleteReminderAccessDenied() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));

        assertThatThrownBy(() -> reminderService.deleteReminder(1L, 2L))
                .isInstanceOf(AccessDeniedException.class);

        verify(reminderRepository, never()).deleteById(any());
    }

    @Test
    void sortRemindersSuccess() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title"));
        Page<Reminder> page = new PageImpl<>(List.of(testReminder));

        when(reminderRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(reminderMapperDto.toDto(testReminder)).thenReturn(responseDto);

        List<ReminderResponseDto> result = reminderService.sortReminders("name", 0, 10, 1L);

        assertThat(result).hasSize(1);
        verify(reminderRepository).findByUserId(eq(1L), any(Pageable.class));
    }


    @Test
    void filterRemindersByDateSuccess() {
        LocalDate date = LocalDate.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reminder> page = new PageImpl<>(List.of(testReminder));

        when(reminderRepository.findByUserIdAndRemindAtDate(eq(1L), eq(date), any(Pageable.class)))
                .thenReturn(page);
        when(reminderMapperDto.toDto(testReminder)).thenReturn(responseDto);

        List<ReminderResponseDto> result = reminderService.filterReminders(date, null, 0, 10, 1L);

        assertThat(result).hasSize(1);
        verify(reminderRepository).findByUserIdAndRemindAtDate(eq(1L), eq(date), any(Pageable.class));
    }

    @Test
    void filterRemindersByTimeSuccess() {
        LocalTime time = LocalTime.of(14, 0);
        Page<Reminder> page = new PageImpl<>(List.of(testReminder));

        when(reminderRepository.findByUserIdAndRemindAtTime(eq(1L), eq(time), any(Pageable.class)))
                .thenReturn(page);
        when(reminderMapperDto.toDto(testReminder)).thenReturn(responseDto);

        List<ReminderResponseDto> result = reminderService.filterReminders(null, time, 0, 10, 1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void listRemindersSuccess() {
        Page<Reminder> page = new PageImpl<>(List.of(testReminder));

        when(reminderRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(reminderMapperDto.toDto(testReminder)).thenReturn(responseDto);

        Page<ReminderResponseDto> result = reminderService.listReminders(0, 10, 1L);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void searchRemindersSuccess() {
        Page<Reminder> page = new PageImpl<>(List.of(testReminder));

        when(reminderRepository.findByUserIdAndTitleContainingOrDescriptionContaining(
                eq(1L), eq("Test"), eq("Test"), any(Pageable.class)
        )).thenReturn(page);
        when(reminderMapperDto.toDto(testReminder)).thenReturn(responseDto);

        List<ReminderResponseDto> result = reminderService.searchReminders("Test", 0, 10, 1L);

        assertThat(result).hasSize(1);
        verify(reminderRepository).findByUserIdAndTitleContainingOrDescriptionContaining(
                eq(1L), eq("Test"), eq("Test"), any(Pageable.class)
        );
    }
}
