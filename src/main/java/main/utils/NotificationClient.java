package main.utils;

import main.utils.client_dtos.CreateUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-system", url = "http://localhost:8081/notification/v1")
public interface NotificationClient {

    @PostMapping("/user")
    void saveUser(@RequestBody CreateUserRequest createUserRequest);
}
