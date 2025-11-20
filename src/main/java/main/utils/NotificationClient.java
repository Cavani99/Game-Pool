package main.utils;

import main.utils.client_dtos.CreateNotificationRequest;
import main.utils.client_dtos.CreateUserRequest;
import main.utils.client_dtos.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "notification-system", url = "http://localhost:8081/notification/v1")
public interface NotificationClient {

    @PostMapping("/user")
    void saveUser(@RequestBody CreateUserRequest createUserRequest);

    @PostMapping("/notification")
    void saveNotification(@RequestBody CreateNotificationRequest createNotificationRequest);

    @GetMapping("/notifications/{id}")
    List<NotificationResponse> getNotifications(@PathVariable("id") UUID userId);

    @GetMapping("/notification/{id}")
    NotificationResponse getNotification(@PathVariable("id") UUID id);

    @DeleteMapping("/notification/{id}")
    ResponseEntity<Void> removeNotification(@PathVariable UUID id);
}
