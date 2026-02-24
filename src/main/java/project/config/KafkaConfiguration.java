package project.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfiguration {

    public static final String USER_ADDED_KAFKA_EVENT = "user-added-event.v1";

    @Bean
    public NewTopic buildNewTopic() {
        return TopicBuilder.name(USER_ADDED_KAFKA_EVENT).build();
    }
}
