package com.devsuperior.dscatalog.controllers;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.exceptions.DataBaseException;
import com.devsuperior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.factories.Factory;
import com.devsuperior.dscatalog.services.ProductService;
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

@WebMvcTest(ProductController.class)
public class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Long EXISTING_ID = 1L;
    private final Long NON_EXISTING_ID = 2L;
    private final long DEPENDENT_ID = 3L;

    private final ProductDTO PRODUCT_DTO = Factory.createProductDTO();
    private final PageImpl<ProductDTO> PAGE = new PageImpl<>(List.of(PRODUCT_DTO));

    @BeforeEach
    public void setup() {
    }


    @Test
    public void findAllShouldReturnPage() throws Exception {
        when(productService.findAllPaged(any())).thenReturn(PAGE);

        mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExist() throws Exception {
        when(productService.findById(EXISTING_ID)).thenReturn(PRODUCT_DTO);

        mockMvc.perform(get("/products/{id}", EXISTING_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EXISTING_ID))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.imgUrl").exists())
                .andExpect(jsonPath("$.price").exists());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        when(productService.findById(NON_EXISTING_ID)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get("/products/{id}", NON_EXISTING_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExist() throws Exception {
        when(productService.update(eq(EXISTING_ID), any())).thenReturn(PRODUCT_DTO);

        String jsonBody = objectMapper.writeValueAsString(PRODUCT_DTO);

        mockMvc.perform(put("/products/{id}", EXISTING_ID)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EXISTING_ID))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.imgUrl").exists())
                .andExpect(jsonPath("$.price").exists())
                .andExpect(jsonPath("$.categories").exists());
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        when(productService.update(eq(NON_EXISTING_ID), any())).thenThrow(ResourceNotFoundException.class);

        String jsonBody = objectMapper.writeValueAsString(PRODUCT_DTO);

        mockMvc.perform(put("/products/{id}", NON_EXISTING_ID)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExist() throws Exception {
        doNothing().when(productService).delete(EXISTING_ID);

        mockMvc.perform(delete("/products/{id}", EXISTING_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        doThrow(ResourceNotFoundException.class).when(productService).delete(NON_EXISTING_ID);

        mockMvc.perform(delete("/products/{id}", NON_EXISTING_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnBadRequestWhenIdHasIntegrityViolation() throws Exception {
        doThrow(DataBaseException.class).when(productService).delete(DEPENDENT_ID);

        mockMvc.perform(delete("/products/{id}", DEPENDENT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void insertShouldReturnProductDTOWhenCalled() throws Exception {
        when(productService.insert(any(ProductDTO.class))).thenReturn(PRODUCT_DTO);

        String jsonBody = objectMapper.writeValueAsString(PRODUCT_DTO);

        mockMvc.perform(post("/products")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.imgUrl").exists())
                .andExpect(jsonPath("$.price").exists())
                .andExpect(jsonPath("$.categories").exists());
    }
}
