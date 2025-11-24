package main.repository;

import main.model.Transaction;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findAllBySenderOrReceiverOrderByCreatedOnDesc(User sender, User receiver);

    List<Transaction> findAllByOrderByCreatedOnDesc();
}
