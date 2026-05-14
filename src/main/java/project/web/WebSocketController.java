package project.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import project.event.payloads.CreateMessageRequest;
import project.event.payloads.CreateNotificationRequest;
import project.event.payloads.NotificationMessage;
import project.service.NotificationService;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class WebSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @MessageMapping("/chat")
    public void handleChatMessage(CreateMessageRequest request) {
        CreateNotificationRequest createNotificationRequest = notificationService.createChatMessageRequest(
                request.getSenderId(),
                request.getReceiverId(),
                request.getMessage()
        );

        NotificationMessage notificationMessage = new NotificationMessage(UUID.randomUUID(),
                request.getMessage(), request.getSenderId(), request.getReceiverId(), LocalDateTime.now());

        send(createNotificationRequest.getSenderEmail(), createNotificationRequest.getReceiverEmail(), notificationMessage);
    }

    public void send(String senderEmail, String receiverEmail, NotificationMessage request) {
        messagingTemplate.convertAndSendToUser(
                senderEmail,
                "/queue/messages",
                request
        );

        messagingTemplate.convertAndSendToUser(
                receiverEmail,
                "/queue/messages",
                request
        );
    }
}
