package org.example.reminderapp.dto.request;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.reminderapp.entity.enums.ReminderType;

import java.time.OffsetDateTime;

@Data
public class ReminderCreateDto {

    @NotBlank(message = "Title is required")
    public String title;
    public String description;
    @NotNull(message = "RemindAt is required")
    @Future(message = "RemindAt must be in future")
    private OffsetDateTime remindAt;

    @NotNull(message = "Type is required")
    private ReminderType type;
}
