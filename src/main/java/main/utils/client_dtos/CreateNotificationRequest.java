package main.utils.client_dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import main.model.NotificationType;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateNotificationRequest {

    @NotNull
    private String title;

    @NotNull
    private String message;

    @URL(message = "Link must be in URL format!")
    private String link;

    @NotNull
    private NotificationType type;

    private UUID senderId;

    private UUID receiverId;
}
