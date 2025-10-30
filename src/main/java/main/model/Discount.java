package main.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "amount")
    @NotNull
    private int amount;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_on")
    @NotNull
    private LocalDateTime createdOn;

    public boolean isDiscountActive() {
        LocalDateTime now = LocalDateTime.now();

        if(amount <= 0) {
            return false;
        }

        if (startDate != null && endDate != null) {
            return !now.isBefore(startDate) && !now.isAfter(endDate);
        }

        if (startDate != null) {
            return !now.isBefore(startDate);
        }

        if (endDate != null) {
            return !now.isAfter(endDate);
        }

        return false;
    }
}
