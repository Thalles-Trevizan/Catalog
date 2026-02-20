package com.devsuperior.dscatalog.controllers;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.exceptions.DataBaseException;
import com.devsuperior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.factories.Factory;
import com.devsuperior.dscatalog.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
public class CategoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Long EXISTING_ID = 1L;
    private final Long NON_EXISTING_ID = 2L;
    private final long DEPENDENT_ID = 3L;

    private final CategoryDTO CATEGORY_DTO = new CategoryDTO(Factory.createCategory());
    private final PageImpl<CategoryDTO> PAGE = new PageImpl<>(List.of(CATEGORY_DTO));

    @BeforeEach
    public void setup() {
    }


    @Test
    public void findAllShouldReturnPage() throws Exception {
        when(categoryService.findAllPaged(any())).thenReturn(PAGE);

        mockMvc.perform(get("/categories").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void findByIdShouldReturnCategoryDTOWhenIdExist() throws Exception {
        when(categoryService.findById(EXISTING_ID)).thenReturn(CATEGORY_DTO);

        mockMvc.perform(get("/categories/{id}", EXISTING_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EXISTING_ID))
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        when(categoryService.findById(NON_EXISTING_ID)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get("/categories/{id}", NON_EXISTING_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateShouldReturnCategoryDTOWhenIdExist() throws Exception {
        when(categoryService.update(eq(EXISTING_ID), any())).thenReturn(CATEGORY_DTO);

        String jsonBody = objectMapper.writeValueAsString(CATEGORY_DTO);

        mockMvc.perform(put("/categories/{id}", EXISTING_ID)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EXISTING_ID))
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        when(categoryService.update(eq(NON_EXISTING_ID), any())).thenThrow(ResourceNotFoundException.class);

        String jsonBody = objectMapper.writeValueAsString(CATEGORY_DTO);

        mockMvc.perform(put("/categories/{id}", NON_EXISTING_ID)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExist() throws Exception {
        doNothing().when(categoryService).delete(EXISTING_ID);

        mockMvc.perform(delete("/categories/{id}", EXISTING_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        doThrow(ResourceNotFoundException.class).when(categoryService).delete(NON_EXISTING_ID);

        mockMvc.perform(delete("/categories/{id}", NON_EXISTING_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnBadRequestWhenIdHasIntegrityViolation() throws Exception {
        doThrow(DataBaseException.class).when(categoryService).delete(DEPENDENT_ID);

        mockMvc.perform(delete("/categories/{id}", DEPENDENT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void insertShouldReturnCategoryDTOWhenCalled() throws Exception {
        when(categoryService.insert(any(CategoryDTO.class))).thenReturn(CATEGORY_DTO);

        String jsonBody = objectMapper.writeValueAsString(CATEGORY_DTO);

        mockMvc.perform(post("/categories")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists());
    }
}
