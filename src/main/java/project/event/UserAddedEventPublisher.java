package project.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import project.event.payloads.CreateUserRequest;

import static project.config.KafkaConfiguration.USER_ADDED_KAFKA_EVENT;

@Component
public class UserAddedEventPublisher {

    private final KafkaTemplate<String, CreateUserRequest> userRequestKafkaTemplate;

    @Autowired
    public UserAddedEventPublisher(KafkaTemplate<String, CreateUserRequest> userRequestKafkaTemplate) {
        this.userRequestKafkaTemplate = userRequestKafkaTemplate;
    }

    public void send(CreateUserRequest request) {
        userRequestKafkaTemplate.send(USER_ADDED_KAFKA_EVENT, request);
    }
}
