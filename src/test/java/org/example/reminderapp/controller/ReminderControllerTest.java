package org.example.reminderapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.reminderapp.dto.request.ReminderCreateDto;
import org.example.reminderapp.dto.request.ReminderUpdateDto;
import org.example.reminderapp.entity.Reminder;
import org.example.reminderapp.entity.User;
import org.example.reminderapp.entity.enums.ReminderType;
import org.example.reminderapp.entity.enums.Role;
import org.example.reminderapp.entity.enums.Status;
import org.example.reminderapp.repository.ReminderRepository;
import org.example.reminderapp.repository.UserRepository;
import org.example.reminderapp.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String token;
    private Reminder testReminder;

    @BeforeEach
    void setUp() {
        reminderRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setFirstname("Test");
        testUser.setLastname("User");
        testUser.setRole(Role.USER);
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser = userRepository.save(testUser);

        token = jwtService.generateToken(testUser);

        testReminder = new Reminder();
        testReminder.setTitle("Test Reminder");
        testReminder.setDescription("Test Description");
        testReminder.setUser(testUser);
        testReminder.setRemindAt(OffsetDateTime.now().plusDays(1));
        testReminder.setType(ReminderType.EMAIL);
        testReminder.setStatus(Status.PENDING);
        testReminder = reminderRepository.save(testReminder);
    }

    @Test
    void findAllRemindersSuccess() throws Exception {
        mockMvc.perform(get("/api/reminders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Reminder"));
    }

    @Test
    void findReminderByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/reminders/" + testReminder.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testReminder.getId()))
                .andExpect(jsonPath("$.title").value("Test Reminder"));
    }

    @Test
    void findReminderByIdUnauthorized() throws Exception {
        mockMvc.perform(get("/api/reminders/" + testReminder.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReminderSuccess() throws Exception {
        ReminderCreateDto createDto = new ReminderCreateDto();
        createDto.setTitle("New Reminder");
        createDto.setDescription("New Description");
        createDto.setRemindAt(OffsetDateTime.now().plusDays(2));
        createDto.setType(ReminderType.EMAIL);

        mockMvc.perform(post("/api/reminders/reminder/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Reminder"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createReminderValidationError() throws Exception {
        ReminderCreateDto createDto = new ReminderCreateDto();

        mockMvc.perform(post("/api/reminders/reminder/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReminderSuccess() throws Exception {
        ReminderUpdateDto updateDto = new ReminderUpdateDto();
        updateDto.setTitle("Updated Title");
        updateDto.setStatus(Status.COMPLETED);

        mockMvc.perform(put("/api/reminders/" + testReminder.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteReminderSuccess() throws Exception {
        mockMvc.perform(delete("/api/reminders/" + testReminder.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReminderNotFound() throws Exception {
        mockMvc.perform(delete("/api/reminders/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // Новые тесты для /v1 endpoints

    @Test
    void sortRemindersByName() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/sort")
                        .param("by", "name")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void sortRemindersByDate() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/sort")
                        .param("by", "date")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void sortRemindersByTime() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/sort")
                        .param("by", "time")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void sortRemindersInvalidField() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/sort")
                        .param("by", "invalid")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void filterRemindersByDate() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/filter")
                        .param("date", LocalDate.now().plusDays(1).toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void filterRemindersByTime() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/filter")
                        .param("time", "14:00:00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void filterRemindersByDateAndTime() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/filter")
                        .param("date", LocalDate.now().plusDays(1).toString())
                        .param("time", "14:00:00")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void filterRemindersWithoutParameters() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/filter")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void listRemindersWithPagination() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/list")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    void listRemindersDefaultPagination() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void searchRemindersByTitle() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/search")
                        .param("query", "Test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchRemindersByDescription() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/search")
                        .param("query", "Description")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void searchRemindersNoResults() throws Exception {
        mockMvc.perform(get("/api/reminders/v1/search")
                        .param("query", "NonExistent")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createReminderUnauthorized() throws Exception {
        ReminderCreateDto createDto = new ReminderCreateDto();
        createDto.setTitle("Unauthorized");

        mockMvc.perform(post("/api/reminders/reminder/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateReminderNotFound() throws Exception {
        ReminderUpdateDto updateDto = new ReminderUpdateDto();
        updateDto.setTitle("Not Found");

        mockMvc.perform(put("/api/reminders/999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReminderAccessDenied() throws Exception {
        User anotherUser = new User();
        anotherUser.setUsername("another");
        anotherUser.setEmail("another@test.com");
        anotherUser.setPassword(passwordEncoder.encode("pass"));
        anotherUser.setRole(Role.USER);
        anotherUser.setCreatedAt(OffsetDateTime.now());
        anotherUser = userRepository.save(anotherUser);

        Reminder anotherReminder = new Reminder();
        anotherReminder.setTitle("Another");
        anotherReminder.setDescription("Another desc");
        anotherReminder.setUser(anotherUser);
        anotherReminder.setRemindAt(OffsetDateTime.now().plusDays(1));
        anotherReminder.setType(ReminderType.EMAIL);
        anotherReminder.setStatus(Status.PENDING);
        anotherReminder = reminderRepository.save(anotherReminder);

        ReminderUpdateDto updateDto = new ReminderUpdateDto();
        updateDto.setTitle("Hack");

        mockMvc.perform(put("/api/reminders/" + anotherReminder.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
}
