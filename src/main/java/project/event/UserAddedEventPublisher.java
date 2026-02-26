package project.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import project.event.payloads.CreateUserRequest;
import project.event.interfaces.UserEventPublisher;

import static project.config.KafkaConfiguration.USER_ADDED_KAFKA_EVENT;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class UserAddedEventPublisher implements UserEventPublisher {

    private final KafkaTemplate<String, CreateUserRequest> userRequestKafkaTemplate;

    @Autowired
    public UserAddedEventPublisher(KafkaTemplate<String, CreateUserRequest> userRequestKafkaTemplate) {
        this.userRequestKafkaTemplate = userRequestKafkaTemplate;
    }

    public void send(CreateUserRequest request) {
        userRequestKafkaTemplate.send(USER_ADDED_KAFKA_EVENT, request);
    }
}
