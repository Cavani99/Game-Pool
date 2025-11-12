package main.service;

import main.model.Game;
import main.model.User;
import main.model.UserRole;
import main.repository.UserRepository;
import main.security.AuthenticationDetails;
import main.web.dto.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User does not exist!"));

        return new AuthenticationDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.getBalance(), user.isBanned());
    }

    public void create(RegisterRequest registerRequest, String avatarPath) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setAvatar(avatarPath);
        user.setRole(registerRequest.getRole());
        user.setBanned(false);
        user.setBalance(BigDecimal.ZERO);
        user.setCreatedOn(LocalDateTime.now());
        user.setUpdatedOn(LocalDateTime.now());

        userRepository.save(user);
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User does not exist!"));
    }

    public List<User> findAllUsers() {
        return userRepository.findAllByRole(UserRole.USER);
    }

    public void changeBanStatus(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User does not exist!"));

        user.setBanned(!user.isBanned());

        userRepository.save(user);
    }

    public void edit(UUID id, EditProfileRequest editProfileRequest, String avatarPath) {
        User user = getById(id);

        user.setUsername(editProfileRequest.getUsername());
        user.setAvatar(avatarPath == null ? user.getAvatar() : avatarPath);
        user.setUpdatedOn(LocalDateTime.now());

        userRepository.save(user);
    }

    public boolean findByUsername(UUID id, String username) {
        Optional<User> user = userRepository.findByUsername(username);

        return user.isPresent() && !user.get().getId().equals(id);
    }

    public void wishlistGame(User user, Game game) {
        List<Game> wishListedGames = user.getWishlistGames();
        boolean isWishListed = user.gameIsWishlisted(game.getId());

        if (isWishListed) {
            wishListedGames.remove(game);
        } else {
            wishListedGames.add(game);
        }
        user.setWishlistGames(wishListedGames);

        userRepository.save(user);
    }

    public void changePassword(UUID id, ChangePasswordRequest changePasswordRequest) {
        User user = getById(id);

        String password = changePasswordRequest.getPassword();
        String passwordRepeat = changePasswordRequest.getRepeat_password();

        if (password.equals(passwordRepeat)) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user);
    }

    public void addFunds(UUID id, AddFundsRequest addFundsRequest) {
        User user = getById(id);

        user.setBalance(user.getBalance().add(addFundsRequest.getAmount()));

        userRepository.save(user);
    }

    public void sendFunds(UUID id, SendFundsRequest sendFundsRequest) {
        User user = getById(id);
        User friend = getById(sendFundsRequest.getFriend());

        user.setBalance(user.getBalance().subtract(sendFundsRequest.getAmount()));
        friend.setBalance(friend.getBalance().add(sendFundsRequest.getAmount()));

        userRepository.save(user);
        userRepository.save(friend);
    }

    public boolean hasFunds(UUID id, BigDecimal amount) {
        User user = getById(id);

        return user.getBalance().compareTo(amount) >= 0;
    }
}
