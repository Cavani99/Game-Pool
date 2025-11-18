package main.service;

import main.model.NotificationType;
import main.model.User;
import main.utils.NotificationClient;
import main.utils.client_dtos.CreateNotificationRequest;
import main.utils.client_dtos.CreateUserRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationClient notificationClient;

    public NotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
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

    public void saveNotification(CreateNotificationRequest createNotificationRequest) {
        notificationClient.saveNotification(createNotificationRequest);
    }
}
