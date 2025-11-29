package main;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.model.*;
import project.repository.TransactionRepository;
import project.service.GameService;
import project.service.TransactionService;
import project.service.UserService;
import project.web.dto.AddFundsRequest;
import project.web.dto.SendFundsRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceUnitTests {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserService userService;
    @Mock
    private GameService gameService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void whenSelfTransactionIsSaved_thenResultIsRight() {
        AddFundsRequest request = AddFundsRequest.builder()
                .amount(BigDecimal.valueOf(20))
                .build();

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userService.getById(userId))
                .thenReturn(user);

        transactionService.createSelfTransaction(userId, request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction savedTransaction = captor.getValue();

        assertEquals(request.getAmount(), savedTransaction.getAmount());
        assertNull(savedTransaction.getSender());
        assertEquals(user, savedTransaction.getReceiver());
        assertEquals("20 € of funds added to your wallet!", savedTransaction.getDescription());
        assertNotNull(savedTransaction.getCreatedOn());
    }

    @Test
    public void whenSendFundsTransactionIsSaved_thenResultIsRight() {
        UUID friendId = UUID.randomUUID();
        User friend = new User();
        friend.setId(friendId);
        friend.setUsername("Martin");

        SendFundsRequest request = SendFundsRequest.builder()
                .amount(BigDecimal.valueOf(15))
                .friend(friendId)
                .build();

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("Ivan");

        when(userService.getById(userId))
                .thenReturn(user);

        when(userService.getById(request.getFriend()))
                .thenReturn(friend);


        transactionService.createSendFundsTransaction(userId, request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction savedTransaction = captor.getValue();

        assertEquals(request.getAmount(), savedTransaction.getAmount());
        assertEquals(user, savedTransaction.getSender());
        assertEquals(friend, savedTransaction.getReceiver());
        assertEquals("15 € of funds sent to Martin", savedTransaction.getDescription());
        assertNotNull(savedTransaction.getCreatedOn());
    }

    @Test
    public void whenGameTransactionIsSaved_thenResultIsRight() {
        UUID gameId = UUID.randomUUID();
        Game game = new Game();
        game.setId(gameId);
        game.setTitle("Game");
        game.setPrice(10.0);

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userService.getById(userId))
                .thenReturn(user);

        when(gameService.getActualPrice(game))
                .thenReturn(10.0);

        transactionService.createBuyGameTransaction(userId, game);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction savedTransaction = captor.getValue();

        assertEquals(BigDecimal.valueOf(gameService.getActualPrice(game)), savedTransaction.getAmount());
        assertEquals(user, savedTransaction.getSender());
        assertNull(savedTransaction.getReceiver());
        assertEquals("Bought Game for 10.0 €!", savedTransaction.getDescription());
        assertNotNull(savedTransaction.getCreatedOn());
    }

    @Test
    public void whenGetByUser_thenGetTransactionsWhereUserIsSenderOrReceiver() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        Transaction transaction1 = new Transaction();
        transaction1.setId(UUID.randomUUID());
        transaction1.setSender(user);
        transaction1.setCreatedOn(LocalDateTime.now());

        Transaction transaction2 = new Transaction();
        transaction2.setId(UUID.randomUUID());
        transaction2.setReceiver(user);
        transaction2.setCreatedOn(LocalDateTime.now().plusHours(5));

        when(transactionRepository.findAllBySenderOrReceiverOrderByCreatedOnDesc(user, user))
                .thenReturn(List.of(transaction2, transaction1));

        List<Transaction> result = transactionService.getByUser(user);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedOn()
                .isAfter(result.get(1).getCreatedOn()));
        assertSame(result.get(0).getReceiver().getId(), userId);
        assertNull(result.get(0).getSender());
        assertSame(result.get(1).getSender().getId(), userId);
        assertNull(result.get(1).getReceiver());
    }

    @Test
    public void transactionFindAllIsSortedRight() {
        Transaction transaction1 = new Transaction();
        transaction1.setId(UUID.randomUUID());
        transaction1.setCreatedOn(LocalDateTime.now());

        Transaction transaction2 = new Transaction();
        transaction2.setId(UUID.randomUUID());
        transaction2.setCreatedOn(LocalDateTime.now().plusHours(5));

        when(transactionRepository.findAllByOrderByCreatedOnDesc())
                .thenReturn(List.of(transaction2, transaction1));

        List<Transaction> result = transactionService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedOn()
                .isAfter(result.get(1).getCreatedOn()));

        verify(transactionRepository).findAllByOrderByCreatedOnDesc();
    }
}
