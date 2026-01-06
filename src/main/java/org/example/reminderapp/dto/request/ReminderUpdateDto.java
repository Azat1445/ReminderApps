package org.example.reminderapp.dto.request;

import lombok.Data;
import org.example.reminderapp.entity.enums.ReminderType;
import org.example.reminderapp.entity.enums.Status;

import java.time.OffsetDateTime;


@Data
public class ReminderUpdateDto {

    private String title;
    private String description;
    private OffsetDateTime remindAt;
    private ReminderType type;
    private Status status;
}
