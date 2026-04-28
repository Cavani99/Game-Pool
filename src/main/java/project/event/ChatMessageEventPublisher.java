package project.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import project.event.interfaces.ChatEventPublisher;
import project.event.payloads.CreateNotificationRequest;

import static project.config.KafkaConfiguration.CHAT_MESSAGE_KAFKA_EVENT;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class ChatMessageEventPublisher implements ChatEventPublisher {
    private final KafkaTemplate<String, CreateNotificationRequest> notificationRequestKafkaTemplate;

    @Autowired
    public ChatMessageEventPublisher(KafkaTemplate<String, CreateNotificationRequest> notificationRequestKafkaTemplate) {
        this.notificationRequestKafkaTemplate = notificationRequestKafkaTemplate;
    }

    public void send(CreateNotificationRequest request) {
        notificationRequestKafkaTemplate.send(CHAT_MESSAGE_KAFKA_EVENT, request);
    }
}
