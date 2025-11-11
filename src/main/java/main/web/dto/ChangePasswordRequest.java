package main.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Field must not be empty")
    @Size(min = 4, message = "Characters must be more than 4 symbols")
    private String password;

    @NotBlank(message = "Field must not be empty")
    @Size(min = 4, message = "Characters must be more than 4 symbols")
    private String repeat_password;
}
