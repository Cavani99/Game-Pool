package project.utils;

import project.event.payloads.CreateNotificationRequest;
import project.event.payloads.CreateUserRequest;
import project.event.payloads.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "notification-system", url = "http://localhost:8081/notification/v1", fallback = NotificationFallback.class)
public interface NotificationClient {

    @PostMapping("/user")
    void saveUser(@RequestBody CreateUserRequest createUserRequest);

    @PostMapping("/notification")
    void saveNotification(@RequestBody CreateNotificationRequest createNotificationRequest);

    @GetMapping("/notifications/{id}")
    List<NotificationResponse> getNotifications(@PathVariable("id") UUID userId);

    @GetMapping("/notifications/title/{title}")
    List<NotificationResponse> getGameDiscountNotifications(@PathVariable("title") String title);

    @GetMapping("/notification/{id}")
    NotificationResponse getNotification(@PathVariable("id") UUID id);

    @DeleteMapping("/notification/{id}")
    ResponseEntity<Void> removeNotification(@PathVariable UUID id);
}
