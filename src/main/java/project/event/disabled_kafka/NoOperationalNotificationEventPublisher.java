package project.event.disabled_kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import project.event.interfaces.NotificationEventPublisher;
import project.event.payloads.CreateNotificationRequest;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOperationalNotificationEventPublisher implements NotificationEventPublisher {

    @Override
    public void send(CreateNotificationRequest request) {
    }
}
