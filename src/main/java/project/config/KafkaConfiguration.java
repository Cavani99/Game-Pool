package project.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(
        name = "kafka.enabled",
        havingValue = "true"
)
public class KafkaConfiguration {

    public static final String USER_ADDED_KAFKA_EVENT = "user-added-event.v1";
    public static final String NOTIFICATION_SAVE_KAFKA_EVENT = "notification-save-event.v1";
    public static final String CHAT_MESSAGE_KAFKA_EVENT = "notification-chat-message-event.v1";

    @Bean
    public NewTopic userAddedTopic() {
        return TopicBuilder.name(USER_ADDED_KAFKA_EVENT).build();
    }

    @Bean
    public NewTopic notificationSaveTopic() {
        return TopicBuilder.name(NOTIFICATION_SAVE_KAFKA_EVENT).build();
    }

    @Bean
    public NewTopic chatMessageTopic() {
        return TopicBuilder.name(CHAT_MESSAGE_KAFKA_EVENT).build();
    }
}
