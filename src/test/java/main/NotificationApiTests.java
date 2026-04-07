package main;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import project.Application;
import project.service.GameService;
import project.service.MessageService;
import project.service.NotificationService;
import project.service.UserService;
import project.event.payloads.NotificationResponse;
import project.web.NotificationsController;

import java.util.Locale;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationsController.class)
@ContextConfiguration(classes = Application.class)
@ActiveProfiles("dev")
@Import(NotificationApiTests.TestConfig.class)
public class NotificationApiTests {

    @TestConfiguration
    static class TestConfig {

        @Bean
        public LocaleResolver localeResolver() {
            AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
            resolver.setDefaultLocale(Locale.ENGLISH);
            return resolver;
        }

        @Bean
        public MessageSource messageSource() {
            ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
            ms.setBasename("messages"); // looks for messages.properties
            ms.setDefaultEncoding("UTF-8");
            ms.setUseCodeAsDefaultMessage(true); // prevents crashes if key missing
            return ms;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private MessageService messageService;

    @Test
    void testSeeNotificationDetails() throws Exception {
        UUID notificationId = UUID.randomUUID();

        NotificationResponse response = new NotificationResponse();
        response.setId(notificationId);
        response.setTitle("Notify");
        response.setMessage("Test message");
        response.setLink("example.com");
        response.setLinkTitle("Open");

        when(notificationService.getNotificationById(notificationId))
                .thenReturn(response);

        mockMvc.perform(get("/dashboard/notifications/details/{id}", notificationId)
                        .with(user("testuser").authorities(new SimpleGrantedAuthority("USER")))
                        .locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(view().name("notification-details"))
                .andExpect(model().attributeExists("notification"))
                .andExpect(model().attribute("page", "notifications"))
                .andExpect(model().attribute("title", "Notifications"));
    }

    @Test
    void testRemoveNotification() throws Exception {
        UUID notificationId = UUID.randomUUID();

        when(notificationService.removeNotification(notificationId))
                .thenReturn(HttpStatus.OK);

        mockMvc.perform(get("/dashboard/notifications/remove/" + notificationId)
                        .with(csrf())
                        .with(user("testuser").authorities(new SimpleGrantedAuthority("USER"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/notifications"));
    }

}
