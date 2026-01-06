package org.example.reminderapp.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.reminderapp.dto.request.UserProfileUpdateDto;
import org.example.reminderapp.entity.User;
import org.example.reminderapp.entity.enums.Role;
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

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User anotherUser;
    private String token;
    private String anotherToken;

    @BeforeEach
    void setUp() {
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

        anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password"));
        anotherUser.setFirstname("Another");
        anotherUser.setLastname("User");
        anotherUser.setRole(Role.USER);
        anotherUser.setCreatedAt(OffsetDateTime.now());
        anotherUser = userRepository.save(anotherUser);

        token = jwtService.generateToken(testUser);
        anotherToken = jwtService.generateToken(anotherUser);
    }

    @Test
    void findAllUsersSuccess() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void findUserByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void findUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentUserProfileSuccess() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getCurrentUserProfileUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUserSuccess() throws Exception {
        UserProfileUpdateDto updateDto = new UserProfileUpdateDto();
        updateDto.setFirstname("Updated");
        updateDto.setLastname("Name");

        mockMvc.perform(put("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Updated"))
                .andExpect(jsonPath("$.lastname").value("Name"));
    }

    @Test
    void updateUserAccessDenied() throws Exception {
        UserProfileUpdateDto updateDto = new UserProfileUpdateDto();
        updateDto.setFirstname("Hacked");

        mockMvc.perform(put("/api/users/" + anotherUser.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUserSuccess() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUserAccessDenied() throws Exception {
        mockMvc.perform(delete("/api/users/" + anotherUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
