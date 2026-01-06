package org.example.reminderapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.reminderapp.entity.enums.Role;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String password;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String firstname;

    private String lastname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reminder> reminders;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = true;
}
