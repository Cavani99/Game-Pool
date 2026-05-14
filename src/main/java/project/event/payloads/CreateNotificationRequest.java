package project.event.payloads;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import project.model.NotificationType;
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

    private String linkTitle;

    @NotNull
    private NotificationType type;

    private UUID senderId;

    private UUID receiverId;

    private String senderEmail;

    private String receiverEmail;
}
