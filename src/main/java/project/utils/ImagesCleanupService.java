package project.utils;

import org.springframework.stereotype.Service;
import project.service.GameService;
import project.service.MessageService;
import project.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ImagesCleanupService {

    private final UserService userService;
    private final GameService gameService;
    private final MessageService messageService;

    public ImagesCleanupService(UserService userService, GameService gameService, MessageService messageService) {
        this.userService = userService;
        this.gameService = gameService;
        this.messageService = messageService;
    }

    private boolean isExternalUrl(String path) {
        try {
            URI uri = new URI(path);
            return uri.getScheme() != null && uri.getScheme().startsWith("http");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public void deleteUnusedUserAvatars(Logger logger, Locale locale) {
        Set<String> usedAvatars = userService.findUsersWithAvatar().stream()
                .filter(path -> !isExternalUrl(path))
                .map(path -> Paths.get(path).getFileName().toString())
                .collect(Collectors.toSet());

        Path avatarDir = Paths.get("uploads/avatars");

        try (Stream<Path> files = Files.list(avatarDir)) {
            files
                    .filter(Files::isRegularFile)
                    .filter(file -> !usedAvatars.contains(file.getFileName().toString()))
                    .forEach(file -> {
                        try {
                            Files.delete(file);

                            String message = messageService.getLocalizedMessage("avatar_deleted", locale);
                            logger.info(message, file);
                        } catch (IOException e) {
                            String message = messageService.getLocalizedMessage("avatar_delete_fail", locale);
                            logger.error(message, file, e);
                        }
                    });
        } catch (IOException e) {
            String message = messageService.getLocalizedMessage("no_files", locale);
            logger.warn(message);
        }
    }

    public void deleteUnusedGameImages(Logger logger, Locale locale) {
        Set<String> usedImages = gameService.findGamesWithImage().stream()
                .filter(path -> !isExternalUrl(path))
                .map(path -> Paths.get(path).getFileName().toString())
                .collect(Collectors.toSet());

        Path avatarDir = Paths.get("uploads/games");

        try (Stream<Path> files = Files.list(avatarDir)) {
            files
                    .filter(Files::isRegularFile)
                    .filter(file -> !usedImages.contains(file.getFileName().toString()))
                    .forEach(file -> {
                        try {
                            Files.delete(file);

                            String message = messageService.getLocalizedMessage("image_deleted", locale);
                            logger.info(message, file);
                        } catch (IOException e) {

                            String message = messageService.getLocalizedMessage("image_delete_fail", locale);
                            logger.error(message, file, e);
                        }
                    });
        } catch (IOException e) {
            String message = messageService.getLocalizedMessage("no_files", locale);
            logger.warn(message);
        }
    }
}
