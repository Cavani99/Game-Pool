package main.repository;

import main.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    List<Game> findAllByOrderByCreatedOnDesc();

    Optional<Game> findByTitle(String title);

    @Query("""
        SELECT g FROM Game g
        WHERE (:categories IS NULL OR g.category.id IN :categories)
           OR (:companies IS NULL OR g.company.id IN :companies)
    """)
    List<Game> findAllByCategoryIdOrCompanyIdList(
            @Param("categories") List<UUID> categories,
            @Param("companies") List<UUID> companies
    );
}
