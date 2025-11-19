package main.utils;

import main.utils.client_dtos.CreateNotificationRequest;
import main.utils.client_dtos.CreateUserRequest;
import main.utils.client_dtos.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}
