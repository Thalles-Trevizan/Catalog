package com.devsuperior.dscatalog.services;


import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.exceptions.DataBaseException;
import com.devsuperior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.factories.Factory;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTests {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    private final long EXISTING_ID = 1L;
    private final long NON_EXISTING_ID = 2L;
    private final long DEPENDENT_ID = 3L;

    private final Category CATEGORY = Factory.createCategory();
    private final CategoryDTO CATEGORY_DTO = new CategoryDTO(CATEGORY);
    private final PageImpl<Category> PAGE = new PageImpl<>(List.of(new Category()));

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    public void findAllPagedShoudReturnPage() {
        when(categoryRepository.findAll((Pageable) any())).thenReturn(PAGE);

        Pageable pageRequest = PageRequest.of(0, 10);
        Page<CategoryDTO> result = categoryService.findAllPaged(pageRequest);

        Assertions.assertNotNull(result);
        verify(categoryRepository, times(1)).findAll(pageRequest);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(categoryRepository.findById(NON_EXISTING_ID)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(NON_EXISTING_ID));

        verify(categoryRepository, times(1)).findById(NON_EXISTING_ID);

    }

    @Test
    public void findByIdShouldReturnCategorydtoWhenIdExists() {
        when(categoryRepository.findById(EXISTING_ID)).thenReturn(Optional.of(CATEGORY));

        CategoryDTO result = categoryService.findById(EXISTING_ID);

        Assertions.assertNotNull(result);
        verify(categoryRepository, times(1)).findById(EXISTING_ID);
    }

    @Test
    public void insertShouldReturnCategoryDTOWhenCalled() {
        when(categoryRepository.save(any())).thenReturn(CATEGORY);

        CategoryDTO result = categoryService.insert(CATEGORY_DTO);

        Assertions.assertNotNull(result);
        verify(categoryRepository, times(1)).save(any());
    }

    @Test
    public void updateShouldReturnCategoryDTOWhenIdExist() {
        when(categoryRepository.getReferenceById(EXISTING_ID)).thenReturn(CATEGORY);
        when(categoryRepository.save(any())).thenReturn(CATEGORY);

        CategoryDTO result = categoryService.update(EXISTING_ID, CATEGORY_DTO);

        Assertions.assertNotNull(result);
        verify(categoryRepository, times(1)).getReferenceById(EXISTING_ID);
        verify(categoryRepository, times(1)).save(any());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(categoryRepository.getReferenceById(NON_EXISTING_ID)).thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(ResourceNotFoundException.class, () -> categoryService.update(NON_EXISTING_ID, CATEGORY_DTO));

        verify(categoryRepository, times(1)).getReferenceById(NON_EXISTING_ID);
    }


    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        when(categoryRepository.findById(EXISTING_ID))
                .thenReturn(Optional.of(CATEGORY));

        doNothing().when(categoryRepository).delete(CATEGORY);

        Assertions.assertDoesNotThrow(() -> categoryService.delete(EXISTING_ID));

        verify(categoryRepository, times(1)).findById(EXISTING_ID);
        verify(categoryRepository, times(1)).delete(CATEGORY);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(categoryRepository.findById(NON_EXISTING_ID))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(NON_EXISTING_ID));

        verify(categoryRepository, times(1)).findById(NON_EXISTING_ID);
        verify(categoryRepository, times(0)).delete(any());
    }

    @Test
    public void deleteShouldThrowDataBaseExceptionWhenDependentId() {
        when(categoryRepository.findById(DEPENDENT_ID))
                .thenReturn(Optional.of(CATEGORY));

        doThrow(DataIntegrityViolationException.class).when(categoryRepository).delete(CATEGORY);

        Assertions.assertThrows(DataBaseException.class, () -> categoryService.delete(DEPENDENT_ID));

        verify(categoryRepository, times(1)).findById(DEPENDENT_ID);
        verify(categoryRepository, times(1)).delete(CATEGORY);
    }
}
