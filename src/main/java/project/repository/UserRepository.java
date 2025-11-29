package project.repository;

import project.model.User;
import project.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    List<User> findAllByRole(UserRole userRole);

    List<User> findAllByWishlistGames_Id(UUID gameId);
}
