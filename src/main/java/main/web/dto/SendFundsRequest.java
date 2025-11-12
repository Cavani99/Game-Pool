package main.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendFundsRequest {

    @NotNull(message = "Please, pick a friend!")
    private UUID friend;

    @Min(value = 1, message = "You must put a number more than 0!")
    private BigDecimal amount;
}
