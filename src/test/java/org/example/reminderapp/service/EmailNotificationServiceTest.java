package org.example.reminderapp.service;

import org.example.reminderapp.entity.User;
import org.example.reminderapp.service.notification.EmailNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailNotificationService, "fromEmail", "test@example.com");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
    }

    @Test
    void sendNotificationSuccess() {
        emailNotificationService.sendNotification(testUser, "Test Title", "Test Message");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationUserWithoutEmail() {
        testUser.setEmail(null);

        emailNotificationService.sendNotification(testUser, "Title", "Message");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationEmailFailure() {
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        emailNotificationService.sendNotification(testUser, "Title", "Message");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
