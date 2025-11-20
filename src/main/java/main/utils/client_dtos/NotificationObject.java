package main.utils.client_dtos;

import lombok.*;
import main.model.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
public class NotificationObject {
    private UUID id;
    private String title;
    private String message;
    private String link;
    private String linkTitle;
    private NotificationType type;
    private LocalDateTime createdOn;
    private String senderUsername;
    private boolean completed;
    private boolean seen;

    public NotificationObject(NotificationResponse notification, String senderUsername) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.link = notification.getLink();
        this.linkTitle = notification.getLinkTitle();
        this.type = notification.getType();
        this.createdOn = notification.getCreatedOn();
        this.senderUsername = senderUsername;
        this.completed = notification.isCompleted();
        this.seen = notification.isSeen();
    }
}
