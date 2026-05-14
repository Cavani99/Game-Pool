package project.event.payloads;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateMessageRequest {
    private UUID senderId;
    private UUID receiverId;
    private String message;
}
