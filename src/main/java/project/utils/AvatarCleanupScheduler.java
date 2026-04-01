package project.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AvatarCleanupScheduler {

    private final ImagesCleanupService imagesCleanupService;
    private final Logger logger;

    public AvatarCleanupScheduler(ImagesCleanupService imagesCleanupService) {
        this.imagesCleanupService = imagesCleanupService;
        this.logger = LoggerFactory.getLogger(AvatarCleanupScheduler.class);
    }

    @Scheduled(cron = "0 0 3 ? * SUN")
    public void scheduledAvatarCleanup() {
        Locale locale = Locale.getDefault();
        imagesCleanupService.deleteUnusedUserAvatars(logger, locale);
    }
}
