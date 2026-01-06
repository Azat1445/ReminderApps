package org.example.reminderapp.job;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reminderapp.entity.Reminder;
import org.example.reminderapp.entity.User;
import org.example.reminderapp.entity.enums.Status;
import org.example.reminderapp.repository.ReminderRepository;
import org.example.reminderapp.service.notification.EmailNotificationService;
import org.example.reminderapp.service.notification.TelegramBotService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderJob implements Job {

    private final ReminderRepository reminderRepository;
    private final EmailNotificationService emailNotificationService;
    private final TelegramBotService telegramBotService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Checking for reminders to send");

        OffsetDateTime now = OffsetDateTime.now();
        List<Reminder> reminders = reminderRepository.findAllByRemindAtBeforeAndStatus(now, Status.PENDING);

        if (reminders.isEmpty()) {
            log.info("No reminders found to send");
            return;
        }

        log.info("Found {} reminders. Processing...", reminders.size());

        reminders.forEach(this::processReminder);
    }

    private void processReminder(Reminder reminder) {
        try {
            User user = reminder.getUser();
            String title = reminder.getTitle();
            String message = buildMessage(reminder);

            switch (reminder.getType()) {
                case EMAIL -> emailNotificationService.sendNotification(user, title, message);
                case TELEGRAM -> telegramBotService.sendNotification(user, title, message);
                case BOSH -> {
                    emailNotificationService.sendNotification(user, title, message);
                    telegramBotService.sendNotification(user, title, message);
                }
            }

            reminder.setStatus(Status.SENT);
            reminderRepository.save(reminder);
            log.info("Successfully sent reminder {}", reminder.getId());
        } catch (Exception e) {
            log.error("Failed to send reminder {}", reminder.getId(), e);
            reminder.setStatus(Status.OVERDUE);
            reminderRepository.save(reminder);
        }
    }

    private String buildMessage(Reminder reminder) {
        return String.format("Привет! Не забудь про: %s\n\n", reminder.getTitle(),
                reminder.getDescription() != null ? reminder.getDescription() : "");
    }
}
