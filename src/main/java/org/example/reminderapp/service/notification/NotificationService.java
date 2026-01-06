package org.example.reminderapp.service.notification;

import org.example.reminderapp.entity.User;

public interface NotificationService {
    void sendNotification(User user, String title, String message);
}
