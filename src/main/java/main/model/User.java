package main.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username", unique = true)
    @NotNull
    private String username;

    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "avatar_url")
    @NotNull
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @NotNull
    private UserRole role;

    @Column(name = "is_banned")
    private boolean isBanned;

    @ManyToMany
    @JoinTable(name = "users_games",
            joinColumns = @JoinColumn(name = "owner_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "game_id", referencedColumnName = "id"))
    private List<Game> games;

    @ManyToMany
    @JoinTable(name = "wishlist",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "game_id", referencedColumnName = "id"))
    private List<Game> wishlistGames;

    @Column(name = "balance")
    @NotNull
    private BigDecimal balance;

    //TODO: Add friends to other users, if there is time
    /*
    @ManyToMany
    @JoinTable(name = "friends",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id", referencedColumnName = "id"))
    private List<User> friends;

    @ManyToMany(mappedBy = "friends")
    private List<User> friendOf = new ArrayList<>();

    public void addFriend(User user) {
        friends.add(user);
        user.getFriends().add(this);
    }

    public List<User> getFriends() {
        return friends;
    }

    public void removeFriend(User user) {
        friends.remove(user);
        user.getFriends().remove(this);
    }*/

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

}
