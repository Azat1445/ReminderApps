package org.example.reminderapp.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateDto {

    private String firstname;
    private String lastname;
    private LocalDate birthDate;

    @Email
    private String email;
}
