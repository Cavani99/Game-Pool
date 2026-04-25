package project.event.interfaces;

import project.event.payloads.CreateNotificationRequest;

public interface ChatEventPublisher {
    void send(CreateNotificationRequest request);
}
