package org.example.reminderapp.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileResponseDto {

    private Long id;
    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private LocalDate birthDate;
}
