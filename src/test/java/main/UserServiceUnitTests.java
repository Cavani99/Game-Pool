package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import project.model.Game;
import project.model.User;
import project.model.UserRole;
import project.repository.UserRepository;
import project.service.GameService;
import project.service.UserService;
import project.web.admin.UsersAdminController;
import project.web.dto.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameService gameService;

    private final PasswordEncoder passwordMatcher = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        userService = new UserService(userRepository, gameService, passwordMatcher);
    }

    @Test
    void whenUsernameExist_thenAuthenticationDetailsIsCreated() {
        String email = "ivan@abv.bg";

        User user = new User();
        user.setEmail(email);
        user.setPassword(String.valueOf(1221));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername(email);

        assertEquals(user.getEmail(), result.getUsername());
        assertEquals(user.getPassword(), result.getPassword());
    }

    @Test
    void whenUsernameDoesNotExist_thenThrowRuntimeException() {
        String missingEmail = "marin@abv.bg";

        when(userRepository.findByEmail(missingEmail))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.loadUserByUsername(missingEmail)
        );

        assertEquals("User does not exist!", exception.getMessage());
    }


    @Test
    void whenUserIsCreated_thenDataIsRight() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Ivan");
        request.setEmail("ivan@abv.bg");
        request.setPassword(String.valueOf(124314));
        request.setRole(UserRole.USER);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.create(request, "/uploads/avatar/me.png");

        assertEquals(request.getUsername(), result.getUsername());
        assertEquals(request.getEmail(), result.getEmail());
        assertNotEquals(request.getPassword(), result.getPassword());
        assertTrue(passwordMatcher.matches(request.getPassword(), result.getPassword()));
        assertEquals(request.getRole(), result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenIdExist_thenGetUser() {
        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);
        user.setUsername("Kris");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getById(id);

        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void whenIdDoesNotExist_thenThrowRuntimeException() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getById(id)
        );

        assertEquals("User does not exist!", exception.getMessage());
    }

    @Test
    void whenFindAllUsers_thenGetOnlyUserRoles() {
        User user = new User();
        user.setUsername("Kris");
        user.setRole(UserRole.USER);

        User user2 = new User();
        user2.setUsername("Kris2");
        user2.setRole(UserRole.ADMIN);

        when(userRepository.findAllByRole(UserRole.USER)).thenReturn(List.of(user));

        List<User> result = userService.findAllUsers();

        assertEquals(1, result.size());
        assertEquals("Kris", result.get(0).getUsername());
        assertEquals(UserRole.USER, result.get(0).getRole());
    }

    @Test
    void whenBanStatusChanged_thenResultIsRight() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUsername("Kris");
        user.setRole(UserRole.USER);
        user.setBanned(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Logger logger = LoggerFactory.getLogger(UsersAdminController.class);
        userService.changeBanStatus(userId, logger);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User request = captor.getValue();
        assertTrue(request.isBanned());
    }

    @Test
    void whenUserIsBanned_thenRemoveBan() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUsername("Kris");
        user.setRole(UserRole.USER);
        user.setBanned(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Logger logger = LoggerFactory.getLogger(UsersAdminController.class);
        userService.changeBanStatus(userId, logger);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User request = captor.getValue();
        assertFalse(request.isBanned());
    }

    @Test
    void whenBanUserIdNotExist_thenThrowException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Logger logger = LoggerFactory.getLogger(UsersAdminController.class);
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.changeBanStatus(userId, logger)
        );

        assertEquals("User does not exist!", exception.getMessage());
    }

    @Test
    public void whenUserEdited_thenVerifyChanges() {
        User user = new User();
        user.setUsername("Ivo");

        UUID userId = UUID.randomUUID();
        EditProfileRequest request = EditProfileRequest.builder()
                .username("Kris")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        userService.edit(userId, request, "uploads/avatar/ava.jpg");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User result = captor.getValue();
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getAvatar(), "uploads/avatar/ava.jpg");


        reset(userRepository);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        userService.edit(userId, request, null);
        captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        result = captor.getValue();
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getAvatar(), "uploads/avatar/ava.jpg");
    }

    @Test
    void whenUsernameExistWithThisId_thenReturnFalseElseTrue() {
        String username = "Ivan";
        String username2 = "Kris";

        UUID userId = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        User user2 = new User();
        user2.setId(userId2);
        user2.setUsername(username2);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(username2)).thenReturn(Optional.of(user2));
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = userService.findByUsername(userId, username);
        boolean result2 = userService.findByUsername(userId, username2);
        boolean result3 = userService.findByUsername(userId2, username2);

        assertFalse(result);
        assertTrue(result2);
        assertFalse(result3);
    }

    @Test
    void whenWishlistGameIsWishlisted_thenRemoveElseAdd() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("Ivan");
        user.setWishlistGames(new ArrayList<>());

        Game game = new Game();
        game.setId(UUID.randomUUID());
        game.setTitle("Game1");

        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        userService.wishlistGame(user, game);


        assertEquals(1, user.getWishlistGames().size());
        assertEquals("Game1", user.getWishlistGames().get(0).getTitle());
        verify(userRepository, times(1)).save(user);

        userService.wishlistGame(user, game);

        assertEquals(0, user.getWishlistGames().size());
        verify(userRepository, times(2)).save(user);
    }

    @Test
    void whenChangePasswordWithMatchingPasswords_thenPasswordUpdated() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("newpass");
        request.setRepeat_password("newpass");

        // Use REAL encoder, not a mock
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(userService, "passwordEncoder", encoder);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword(userId, request);

        assertNotNull(user.getPassword());
        assertTrue(encoder.matches("newpass", user.getPassword()));
        verify(userRepository).save(user);
    }

    @Test
    void whenChangePasswordWithDifferentPasswords_thenNothingSaved() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("pass1");
        request.setRepeat_password("pass2");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changePassword(userId, request);

        verify(userRepository, never()).save(any());
    }

    @Test
    void whenAddFunds_thenBalanceIncreases() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setBalance(BigDecimal.valueOf(100));

        AddFundsRequest request = new AddFundsRequest(BigDecimal.valueOf(50));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.addFunds(userId, request);

        assertEquals(BigDecimal.valueOf(150), user.getBalance());
        verify(userRepository).save(user);
    }

    @Test
    void whenSendFunds_thenMoneyTransferredBetweenUsers() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        User sender = new User();
        sender.setId(senderId);
        sender.setBalance(BigDecimal.valueOf(100));

        User receiver = new User();
        receiver.setId(receiverId);
        receiver.setBalance(BigDecimal.valueOf(20));

        SendFundsRequest request = SendFundsRequest.builder()
                .amount(BigDecimal.valueOf(30))
                .friend(receiverId)
                .build();

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        userService.sendFunds(senderId, request);

        assertEquals(BigDecimal.valueOf(70), sender.getBalance());
        assertEquals(BigDecimal.valueOf(50), receiver.getBalance());

        verify(userRepository).save(sender);
        verify(userRepository).save(receiver);
    }

    @Test
    void whenUserHasEnoughFunds_thenReturnTrue() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setBalance(BigDecimal.valueOf(100));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertTrue(userService.hasFunds(userId, BigDecimal.valueOf(50)));
        assertFalse(userService.hasFunds(userId, BigDecimal.valueOf(150)));
    }

    @Test
    void whenUserHasNotEnoughFunds_thenReturnFalse() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setBalance(BigDecimal.valueOf(30));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertFalse(userService.hasFunds(userId, BigDecimal.valueOf(50)));
    }

    @Test
    void whenUserNotFriend_thenReturnTrue() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        User user = new User();
        user.setFriends(new ArrayList<>());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertTrue(userService.userNotFriend(userId, otherId));
    }

    @Test
    void whenUserIsFriend_thenReturnFalse() {
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        User friend = new User();
        friend.setId(friendId);

        User user = new User();
        user.setFriends(List.of(friend));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertFalse(userService.userNotFriend(userId, friendId));
    }

    @Test
    void whenAddFriend_thenFriendAdded() {
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        User user = new User();
        user.setFriends(new ArrayList<>());

        User friend = new User();
        friend.setId(friendId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));

        userService.addFriend(userId, friendId);

        assertEquals(1, user.getFriends().size());
        assertEquals(friend, user.getFriends().get(0));
        verify(userRepository).save(user);
    }

    @Test
    void whenRemoveFriend_thenFriendRemoved() {
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        User friend = new User();
        friend.setId(friendId);

        User user = new User();
        user.setFriends(new ArrayList<>(List.of(friend)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));

        userService.removeFriend(userId, friendId);

        assertEquals(0, user.getFriends().size());
        verify(userRepository).save(user);
    }

    @Test
    void whenUserHasFundsForGame_thenReturnTrue() {
        User user = new User();
        user.setBalance(BigDecimal.valueOf(100));

        Game game = new Game();

        when(gameService.getActualPrice(game)).thenReturn(50.0);

        assertTrue(userService.hasFundsForGame(user, game));
    }

    @Test
    void whenUserDoesNotHaveFundsForGame_thenReturnFalse() {
        User user = new User();
        user.setBalance(BigDecimal.valueOf(20));

        Game game = new Game();

        when(gameService.getActualPrice(game)).thenReturn(30.0);

        assertFalse(userService.hasFundsForGame(user, game));
    }

    @Test
    void whenBuyGame_thenGameAddedAndBalanceReduced() {
        User user = new User();
        user.setBalance(BigDecimal.valueOf(100));
        user.setGames(new ArrayList<>());

        Game game = new Game();

        when(gameService.getActualPrice(game)).thenReturn(40.0);
        when(userRepository.save(user)).thenReturn(user);

        userService.buyGame(user, game);

        assertEquals(1, user.getGames().size());
        assertEquals(BigDecimal.valueOf(60.0), user.getBalance());
        verify(userRepository).save(user);
    }

    @Test
    void whenFindAllWishlistedUsersByGameId_thenReturnUsers() {
        UUID gameId = UUID.randomUUID();

        List<User> users = List.of(new User(), new User());

        when(userRepository.findAllByWishlistGames_Id(gameId)).thenReturn(users);

        List<User> result = userService.findAllWishlistedUsersByGameId(gameId);

        assertEquals(2, result.size());
        assertSame(users, result);
    }

}
