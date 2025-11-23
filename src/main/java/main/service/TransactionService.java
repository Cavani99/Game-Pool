package main.service;

import main.model.Game;
import main.model.Transaction;
import main.model.User;
import main.repository.TransactionRepository;
import main.web.dto.AddFundsRequest;
import main.web.dto.SendFundsRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    private final GameService gameService;

    public TransactionService(TransactionRepository transactionRepository, UserService userService, GameService gameService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.gameService = gameService;
    }

    public void createSelfTransaction(UUID id, AddFundsRequest addFundsRequest) {
        User user = userService.getById(id);

        Transaction transaction = new Transaction();
        transaction.setDescription(addFundsRequest.getAmount() + " € of funds added to your wallet!");
        transaction.setAmount(addFundsRequest.getAmount());
        transaction.setReceiver(user);
        transaction.setCreatedOn(LocalDateTime.now());

        transactionRepository.save(transaction);
    }

    public void createSendFundsTransaction(UUID id, SendFundsRequest sendFundsRequest) {
        User user = userService.getById(id);
        User friend = userService.getById(sendFundsRequest.getFriend());

        Transaction transaction = new Transaction();
        transaction.setDescription(sendFundsRequest.getAmount() + " € of funds sent to " + friend.getUsername());
        transaction.setAmount(sendFundsRequest.getAmount());
        transaction.setSender(user);
        transaction.setReceiver(friend);
        transaction.setCreatedOn(LocalDateTime.now());

        transactionRepository.save(transaction);
    }

    public void createBuyGameTransaction(UUID id, Game game) {
        User user = userService.getById(id);

        Transaction transaction = new Transaction();
        transaction.setDescription("Bought " + game.getTitle() + " for " + gameService.getActualPrice(game) + " €!");
        transaction.setAmount(BigDecimal.valueOf(gameService.getActualPrice(game)));
        transaction.setSender(user);
        transaction.setCreatedOn(LocalDateTime.now());

        transactionRepository.save(transaction);
    }
}
