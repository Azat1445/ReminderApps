package org.example.reminderapp.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reminderapp.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendNotification(User user, String title, String message) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("User {} has no email, skipping email notification", user.getId());
            return;
        }

        try {
            log.info("Sending email to {}", user);
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject("Напоминание: " + title);
            mailMessage.setText(message);

            mailSender.send(mailMessage);
            log.info("Email sent successfully" + user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email to {}", user.getEmail(), e);
        }
    }
}

