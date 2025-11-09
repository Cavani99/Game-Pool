package main.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileRequest {

    @NotBlank(message = "Please, write a username")
    @Size(min = 4, max = 24, message = "Characters must be between 4 and 24 symbols")
    private String username;

    private MultipartFile avatar;

    private String avatarPath;
}
