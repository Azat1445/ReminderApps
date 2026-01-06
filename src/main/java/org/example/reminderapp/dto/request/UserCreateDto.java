package org.example.reminderapp.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserCreateDto {

    private String username;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private LocalDate birthDate;
}
