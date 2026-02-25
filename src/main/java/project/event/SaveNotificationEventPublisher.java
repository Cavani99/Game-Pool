package project.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import project.event.payloads.CreateNotificationRequest;

import static project.config.KafkaConfiguration.NOTIFICATION_SAVE_KAFKA_EVENT;

@Component
public class SaveNotificationEventPublisher {
    private final KafkaTemplate<String, CreateNotificationRequest> notificationRequestKafkaTemplate;

    @Autowired
    public SaveNotificationEventPublisher(KafkaTemplate<String, CreateNotificationRequest> notificationRequestKafkaTemplate) {
        this.notificationRequestKafkaTemplate = notificationRequestKafkaTemplate;
    }

    public void send(CreateNotificationRequest request) {
        notificationRequestKafkaTemplate.send(NOTIFICATION_SAVE_KAFKA_EVENT, request);
    }
}
