package main.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", unique = true)
    @NotNull
    private String title;

    @Column(name = "description")
    @NotNull
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @Column(name = "image")
    private String image;

    @Column(name = "price", nullable = false)
    private Double price;

    @OneToOne
    @JoinColumn(name = "discount_id", referencedColumnName = "id")
    private Discount discount;

    @ManyToMany(mappedBy = "games")
    private Set<User> owners;

    @ManyToMany(mappedBy = "wishlistGames")
    private Set<User> usersWishlisted;

    @Column(name = "created_on")
    @NotNull
    private LocalDateTime createdOn;

    @Column(name = "updated_on")
    @NotNull
    private LocalDateTime updatedOn;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Game game)) return false;

        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Double getPromoPrice() {
        if (this.discount == null) {
            return 0.0;
        }

        int amount = this.discount.getAmount();

        if (this.discount.getType().equals(DiscountType.FIXED) && amount >= this.price) {
            return 0.0;
        }

        if (this.discount.getType().equals(DiscountType.FIXED)) {
            return this.price - amount;
        } else {
            double percent = amount / 100.0;
            double result = price - price * percent;
            return Math.round(result * 100.0) / 100.0;
        }
    }
}
