package com.devsuperior.dscatalog.services;


import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.exceptions.DataBaseException;
import com.devsuperior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.factories.Factory;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
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
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private final long EXISTING_ID = 1L;
    private final long NON_EXISTING_ID = 1000L;
    private final long DEPENDENT_ID = 1001L;

    private final Product PRODUCT = Factory.createProduct();
    private final Category CATEGORY = Factory.createCategory();
    private final ProductDTO PRODUCT_DTO = new ProductDTO(PRODUCT, Set.of(CATEGORY));
    private final PageImpl<Product> PAGE = new PageImpl<>(List.of(new Product()));

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    public void insertShouldReturnProductDTOWhenCalled() {
        when(productRepository.save(any())).thenReturn(PRODUCT);

        ProductDTO result = productService.insert(PRODUCT_DTO);

        Assertions.assertNotNull(result);
        verify(productRepository, times(1)).save(any());
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExist() {
        when(productRepository.getReferenceById(EXISTING_ID)).thenReturn(PRODUCT);
        when(productRepository.save(any())).thenReturn(PRODUCT);
        when(categoryRepository.getReferenceById(any())).thenReturn(CATEGORY);

        ProductDTO result = productService.update(EXISTING_ID, PRODUCT_DTO);

        Assertions.assertNotNull(result);
        verify(productRepository, times(1)).getReferenceById(EXISTING_ID);
        verify(productRepository, times(1)).save(any());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(productRepository.getReferenceById(NON_EXISTING_ID)).thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(ResourceNotFoundException.class, () -> productService.update(NON_EXISTING_ID, PRODUCT_DTO));

        verify(productRepository, times(1)).getReferenceById(NON_EXISTING_ID);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(productRepository.findById(NON_EXISTING_ID)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> productService.findById(NON_EXISTING_ID));

        verify(productRepository, times(1)).findById(NON_EXISTING_ID);

    }

    @Test
    public void findByIdShouldReturnProductdtoWhenIdExists() {
        when(productRepository.findById(EXISTING_ID)).thenReturn(Optional.of(PRODUCT));

        ProductDTO result = productService.findById(EXISTING_ID);

        Assertions.assertNotNull(result);
        verify(productRepository, times(1)).findById(EXISTING_ID);
    }

    @Test
    public void findAllPagedShoudReturnPage() {
        when(productRepository.findAll((Pageable) any())).thenReturn(PAGE);

        Pageable pageRequest = PageRequest.of(0, 10);
        Page<ProductDTO> result = productService.findAllPaged(pageRequest);

        Assertions.assertNotNull(result);
        verify(productRepository, times(1)).findAll(pageRequest);
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        when(productRepository.findById(EXISTING_ID))
                .thenReturn(Optional.of(PRODUCT));

        doNothing().when(productRepository).delete(PRODUCT);

        Assertions.assertDoesNotThrow(() -> productService.delete(EXISTING_ID));

        verify(productRepository, times(1)).findById(EXISTING_ID);
        verify(productRepository, times(1)).delete(PRODUCT);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(productRepository.findById(NON_EXISTING_ID))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> productService.delete(NON_EXISTING_ID));

        verify(productRepository, times(1)).findById(NON_EXISTING_ID);
        verify(productRepository, times(0)).delete(any());
    }

    @Test
    public void deleteShouldThrowDataBaseExceptionWhenDependentId() {
        when(productRepository.findById(DEPENDENT_ID))
                .thenReturn(Optional.of(PRODUCT));

        doThrow(DataIntegrityViolationException.class).when(productRepository).delete(PRODUCT);

        Assertions.assertThrows(DataBaseException.class, () -> productService.delete(DEPENDENT_ID));

        verify(productRepository, times(1)).findById(DEPENDENT_ID);
        verify(productRepository, times(1)).delete(PRODUCT);
    }
}
