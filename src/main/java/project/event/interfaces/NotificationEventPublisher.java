package project.event.interfaces;

import project.event.payloads.CreateNotificationRequest;

public interface NotificationEventPublisher {
    void send(CreateNotificationRequest request);
}
