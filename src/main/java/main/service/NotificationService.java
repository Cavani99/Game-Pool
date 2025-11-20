package main.service;

import main.model.Game;
import main.model.NotificationType;
import main.model.User;
import main.utils.NotificationClient;
import main.utils.client_dtos.CreateNotificationRequest;
import main.utils.client_dtos.CreateUserRequest;
import main.utils.client_dtos.NotificationObject;
import main.utils.client_dtos.NotificationResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationClient notificationClient;
    private final UserService userService;

    public NotificationService(NotificationClient notificationClient, UserService userService) {
        this.notificationClient = notificationClient;
        this.userService = userService;
    }

    public void saveUser(UUID userId) {
        CreateUserRequest createUserRequest = new CreateUserRequest(userId);
        notificationClient.saveUser(createUserRequest);
    }

    public void createFriendInvite(User user, UUID invitedUserId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle("Friend Invitation!");
        request.setMessage("You got a friend invitation from " + user.getUsername() + "!");
        request.setType(NotificationType.REQUEST);
        request.setLink("localhost:8080/dashboard/friends/accept_request/" + user.getId());
        request.setSenderId(user.getId());
        request.setReceiverId(invitedUserId);
        notificationClient.saveNotification(request);
    }

    public void createGameDiscountNotifications(Game game, List<User> users) {
        for (User user : users) {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTitle("Wishlisted Game got discounted!");
            request.setMessage("Game " + game.getTitle() + " got a discount!");
            request.setType(NotificationType.INFORMATION);
            request.setLink("localhost:8080/dashboard/games/details/" + game.getId());
            request.setReceiverId(user.getId());
            notificationClient.saveNotification(request);
        }
    }

    public List<NotificationObject> getNotificationsByUser(UUID userId) {
        List<NotificationResponse> notifications = notificationClient.getNotifications(userId);

        return notifications.stream()
                .map(notification -> {
                    if (notification.getSender() != null) {
                        User sender = userService.getById(notification.getSender());
                        return new NotificationObject(notification, sender.getUsername());
                    } else {
                        return new NotificationObject(notification, null);
                    }
                })
                .toList();
    }

    public NotificationResponse getNotificationById(UUID id) {
        return notificationClient.getNotification(id);
    }

    public HttpStatusCode removeNotification(UUID id) {
        ResponseEntity<Void> response = notificationClient.removeNotification(id);

        return response.getStatusCode();
    }

    public void saveNotification(CreateNotificationRequest createNotificationRequest) {
        notificationClient.saveNotification(createNotificationRequest);
    }
}
