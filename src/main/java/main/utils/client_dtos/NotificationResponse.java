package main.utils.client_dtos;

import lombok.*;
import main.model.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private String title;
    private String message;
    private String link;
    private NotificationType type;
    private LocalDateTime createdOn;
    private UUID sender;
}
