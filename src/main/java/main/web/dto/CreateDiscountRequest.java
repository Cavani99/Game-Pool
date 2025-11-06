package main.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import main.model.DiscountType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateDiscountRequest {

    @NotNull(message = "Please, write an amount")
    private int amount;

    @NotNull(message = "Please, pick a type")
    private DiscountType type;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
}
