package project.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class GameImageCleanupScheduler {

    private final ImagesCleanupService imagesCleanupService;
    private final Logger logger;

    public GameImageCleanupScheduler(ImagesCleanupService imagesCleanupService) {
        this.imagesCleanupService = imagesCleanupService;
        this.logger = LoggerFactory.getLogger(GameImageCleanupScheduler.class);
    }

    @Scheduled(cron = "0 0 4 ? * SUN")
    public void scheduledGameImagesCleanup() {
        Locale locale = Locale.getDefault();
        imagesCleanupService.deleteUnusedGameImages(logger, locale);
    }
}
