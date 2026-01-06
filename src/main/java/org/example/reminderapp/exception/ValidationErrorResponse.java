package org.example.reminderapp.exception;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;
}
