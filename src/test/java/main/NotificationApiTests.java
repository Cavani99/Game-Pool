package main;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.Application;
import project.service.GameService;
import project.service.NotificationService;
import project.service.UserService;
import project.event.payloads.NotificationResponse;
import project.web.NotificationsController;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationsController.class)
@ContextConfiguration(classes = Application.class)
public class NotificationApiTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void testSeeNotificationDetails() throws Exception {
        UUID notificationId = UUID.randomUUID();
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId(notificationId);
        notificationResponse.setTitle("Notify");
        notificationResponse.setLinkTitle("Test");
        notificationResponse.setLink("test");

        when(notificationService.getNotificationById(notificationId))
                .thenReturn(notificationResponse);

        mockMvc.perform(get("/dashboard/notifications/details/" + notificationId)
                        .with(user("testuser").authorities(new SimpleGrantedAuthority("USER"))))
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
