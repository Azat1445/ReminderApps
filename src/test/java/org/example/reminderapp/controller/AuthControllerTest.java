package org.example.reminderapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.reminderapp.dto.request.LoginRequestDto;
import org.example.reminderapp.dto.request.UserCreateDto;
import org.example.reminderapp.entity.User;
import org.example.reminderapp.entity.enums.Role;
import org.example.reminderapp.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerSuccess() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setUsername("newuser");
        createDto.setEmail("newuser@example.com");
        createDto.setPassword("password123");
        createDto.setFirstname("New");
        createDto.setLastname("User");
        createDto.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void registerUsernameAlreadyExists() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setRole(Role.USER);
        existingUser.setCreatedAt(OffsetDateTime.now());
        userRepository.save(existingUser);

        UserCreateDto createDto = new UserCreateDto();
        createDto.setUsername("existinguser");
        createDto.setEmail("new@example.com");
        createDto.setPassword("password123");
        createDto.setFirstname("New");
        createDto.setLastname("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void loginSuccess() throws Exception {
        User user = new User();
        user.setUsername("loginuser");
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUsername("loginuser");
        loginDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void loginInvalidCredentials() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUsername("nonexistent");
        loginDto.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }
}