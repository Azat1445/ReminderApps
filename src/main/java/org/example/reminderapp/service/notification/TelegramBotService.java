package org.example.reminderapp.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.example.reminderapp.entity.User;
import org.example.reminderapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Slf4j
@Component
public class TelegramBotService extends TelegramLongPollingBot implements NotificationService {

    private final UserRepository userRepository;
    private final String botUsername;

    public TelegramBotService(
            UserRepository userRepository,
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.name}") String botUsername
    ) {
        super(botToken);
        this.userRepository = userRepository;
        this.botUsername = botUsername;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (text.startsWith("/start")) {
            sendTelegramMessage(chatId, "Привет! Чтобы получать уведомления, привяжи аккаунт командой:\n/link твой@email.com");
        } else if (text.startsWith("/link ")) {
            String email = text.substring(6).trim();
            linkUser(chatId, email);
        } else {
            sendTelegramMessage(chatId, "Я понимаю только команды /start и /link");
        }
    }

    private void linkUser(Long chatId, String email) {
        Optional<User> userOpt = userRepository.findByEmail(email); // Убедись, что такой метод есть в репозитории!

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setTelegramChatId(chatId);
            userRepository.save(user);
            sendTelegramMessage(chatId, "Аккаунт успешно привязан! Теперь сюда будут приходить напоминания.");
            log.info("User {} linked to Telegram Chat ID {}", email, chatId);
        } else {
            sendTelegramMessage(chatId, "Пользователь с email " + email + " не найден.");
        }
    }

    @Override
    public void sendNotification(User user, String title, String message) {
        if (user.getTelegramChatId() == null) {
            log.debug("User {} has no linked Telegram chat, skipping.", user.getEmail());
            return;
        }

        String fullMessage = "🔔 *" + title + "*\n\n" + message;
        sendTelegramMessage(user.getTelegramChatId(), fullMessage);
    }

    private void sendTelegramMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
         message.setParseMode("Markdown");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send telegram message to {}", chatId, e);
        }
    }
}
