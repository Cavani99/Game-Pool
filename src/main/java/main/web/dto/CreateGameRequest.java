package main.web.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGameRequest {
    @NotBlank(message = "Please, write a title")
    @Size(max = 24, message = "Characters must be at least 24 symbols")
    private String title;

    @NotBlank(message = "Please, write a description")
    private String description;

    private MultipartFile image;

    @NotNull(message = "You must pick a category")
    private UUID categoryId;

    @NotNull(message = "You must pick a category")
    private UUID companyId;

    private String imagePath;

    @NotNull(message = "You must set a price")
    @DecimalMax(value = "300.00", message = "The Price must not be over 300 €")
    @DecimalMin(value = "10.00", message = "The Price must be over 10 €")
    private Double price;
}
