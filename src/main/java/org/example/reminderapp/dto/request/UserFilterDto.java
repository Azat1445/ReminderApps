package org.example.reminderapp.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserFilterDto {

    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private LocalDate birthDate;
}
