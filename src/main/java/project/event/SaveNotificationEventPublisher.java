package project.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import project.event.interfaces.NotificationEventPublisher;
import project.event.payloads.CreateNotificationRequest;

import static project.config.KafkaConfiguration.NOTIFICATION_SAVE_KAFKA_EVENT;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class SaveNotificationEventPublisher implements NotificationEventPublisher {
    private final KafkaTemplate<String, CreateNotificationRequest> notificationRequestKafkaTemplate;

    @Autowired
    public SaveNotificationEventPublisher(KafkaTemplate<String, CreateNotificationRequest> notificationRequestKafkaTemplate) {
        this.notificationRequestKafkaTemplate = notificationRequestKafkaTemplate;
    }

    public void send(CreateNotificationRequest request) {
        notificationRequestKafkaTemplate.send(NOTIFICATION_SAVE_KAFKA_EVENT, request);
    }
}
