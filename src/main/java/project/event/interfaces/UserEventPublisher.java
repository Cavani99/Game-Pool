package project.event.interfaces;

import project.event.payloads.CreateUserRequest;

public interface UserEventPublisher {
    void send(CreateUserRequest request);
}
