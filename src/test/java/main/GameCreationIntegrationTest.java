package main;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.Application;
import project.model.Category;
import project.model.Company;
import project.service.CategoryService;
import project.service.CompanyService;
import project.service.GameService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;


@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GameCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private GameService gameService;
    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private CompanyService companyService;

    @Test
    void createGame_withValidData_returnsRedirect() throws Exception {
        UUID categoryId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);
        Company company = new Company();
        company.setId(companyId);

        when(categoryService.findAll()).thenReturn(List.of(category));
        when(companyService.findAll()).thenReturn(List.of(company));
        when(categoryService.findById(categoryId)).thenReturn(category);
        when(companyService.findById(companyId)).thenReturn(company);
        when(gameService.create(any(), any(), any(), any())).thenReturn(true);

        MockMultipartFile imageFile = new MockMultipartFile("image", "game.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy image content".getBytes());

        mockMvc.perform(multipart("/admin/games/add")
                        .file(imageFile)
                        .param("title", "New Game")
                        .param("description", "Best Game")
                        .param("price", "59.99")
                        .param("categoryId", categoryId.toString())
                        .param("companyId", companyId.toString())
                        .with(csrf())
                        .with(user("admin").authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(redirectedUrl("/admin/games"));
    }

    @Test
    void createGame_withEmptyImage_returnsFormWithError() throws Exception {
        UUID categoryId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);
        Company company = new Company();
        company.setId(companyId);
        when(categoryService.findAll()).thenReturn(List.of(category));
        when(companyService.findAll()).thenReturn(List.of(company));
        mockMvc.perform(post("/admin/games/add")
                        .param("title", "New Game")
                        .param("description", "Best Game")
                        .param("price", "59.99")
                        .param("categoryId", categoryId.toString())
                        .param("companyId", companyId.toString())
                        .with(csrf())
                        .with(user("admin").authorities(new SimpleGrantedAuthority("ADMIN"))))
                        .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("game", "image"))
                .andExpect(view().name("admin/game_form"));
    }
}
