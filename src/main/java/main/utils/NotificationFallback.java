package main.utils;

import main.utils.client_dtos.CreateNotificationRequest;
import main.utils.client_dtos.CreateUserRequest;
import main.utils.client_dtos.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class NotificationFallback implements NotificationClient {
    @Override
    public void saveUser(CreateUserRequest createUserRequest) {
    }

    @Override
    public void saveNotification(CreateNotificationRequest createNotificationRequest) {
    }

    @Override
    public List<NotificationResponse> getNotifications(UUID userId) {
        return Collections.emptyList();
    }

    @Override
    public List<NotificationResponse> getGameDiscountNotifications(String title) {
        return Collections.emptyList();
    }

    @Override
    public NotificationResponse getNotification(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> removeNotification(UUID id) {
        return ResponseEntity.notFound().build();
    }
}
