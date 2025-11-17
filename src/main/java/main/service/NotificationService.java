package main.service;

import main.utils.NotificationClient;
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
}
