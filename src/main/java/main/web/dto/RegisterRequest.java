package main.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import main.model.UserRole;
import org.hibernate.validator.constraints.URL;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Please, write a username")
    @Size(min = 4, max = 24, message = "Characters must be between 4 and 24 symbols")
    private String username;

    @NotBlank
    @Email(message = "Please, write a valid email")
    private String email;

    @NotBlank(message = "Please, set a password")
    @Size(min = 4, message = "Characters must be more than 4 symbols")
    private String password;

    @NotBlank(message = "URL must be set")
    @URL(message = "Please, write a valid url")
    private String avatarUrl;

    @NotNull(message = "You must pick a role")
    private UserRole role;
}
