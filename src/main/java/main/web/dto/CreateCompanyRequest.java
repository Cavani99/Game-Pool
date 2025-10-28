package main.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCompanyRequest {
    @NotBlank(message = "Please, write company name")
    @Size(min = 3, message = "Company must have minimum 3 characters")
    private String name;
}
