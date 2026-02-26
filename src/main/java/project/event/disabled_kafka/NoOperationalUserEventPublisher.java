package project.event.disabled_kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import project.event.interfaces.UserEventPublisher;
import project.event.payloads.CreateUserRequest;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOperationalUserEventPublisher implements UserEventPublisher {

    @Override
    public void send(CreateUserRequest request) {

    }
}
