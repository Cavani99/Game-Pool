package project.utils;

import org.springframework.stereotype.Service;
import project.service.GameService;
import project.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.slf4j.Logger;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ImagesCleanupService {

    private final UserService userService;
    private final GameService gameService;

    public ImagesCleanupService(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    private boolean isExternalUrl(String path) {
        try {
            URI uri = new URI(path);
            return uri.getScheme() != null && uri.getScheme().startsWith("http");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public void deleteUnusedUserAvatars(Logger logger) {
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
                            System.out.println("Deleted unused avatar: " + file);
                        } catch (IOException e) {
                            logger.error("Failed to delete avatar: " + file, e);
                        }
                    });
        } catch (IOException e) {
            logger.warn("No files in this directory!");
        }
    }

    public void deleteUnusedGameImages(Logger logger) {
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
                            System.out.println("Deleted unused image: " + file);
                        } catch (IOException e) {
                            logger.error("Failed to delete image: " + file, e);
                        }
                    });
        } catch (IOException e) {
            logger.warn("No files in this directory!");
        }
    }
}
