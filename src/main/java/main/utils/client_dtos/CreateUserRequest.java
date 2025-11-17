package main.utils.client_dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {

    @NotNull
    private UUID id;
}