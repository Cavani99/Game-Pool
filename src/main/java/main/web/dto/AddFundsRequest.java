package main.web.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddFundsRequest {

    @Min(value = 1, message = "You must put a number more than 0")
    private BigDecimal amount;
}
