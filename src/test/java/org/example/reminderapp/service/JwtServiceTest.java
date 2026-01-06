package org.example.reminderapp.service;

import io.jsonwebtoken.Claims;
import org.example.reminderapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "thisIsAVeryLongSecretKeyThatIsDefinitelyMoreThanSixtyFourBytesLongToSatisfyHsFiveTwelveSecurityRequirementsAndItShouldWorkNowForSure1234567890");
        ReflectionTestUtils.setField(jwtService, "jwtExpiratio", 86400000L);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    void generateTokenSuccess() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void extractUsernameSuccess() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void isTokenValidSuccess() {
        String token = jwtService.generateToken(testUser);

        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User(
                        "testuser", "password", java.util.Collections.emptyList()
                );

        boolean isValid = jwtService.isTokenValid(token, springUser);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValidInvalidUser() {
        String token = jwtService.generateToken(testUser);

        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User(
                        "wronguser", "password", java.util.Collections.emptyList()
                );

        boolean isValid = jwtService.isTokenValid(token, springUser);

        assertThat(isValid).isFalse();
    }
}

