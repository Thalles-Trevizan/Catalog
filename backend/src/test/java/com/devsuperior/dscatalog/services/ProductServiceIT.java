package com.devsuperior.dscatalog.services;


import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.factories.Factory;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ProductServiceIT {

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repository;

    private final long EXISTING_ID = 1L;
    private final long NON_EXISTING_ID = 2000L;
    private long COUNT_TOTAL_PRODUCTS = 25L;

    @Test
    public void findAllPagedShouldReturnPageWhenPage0Size10() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ProductDTO> result = service.findAllPaged(pageRequest);

        Assertions.assertTrue(result.hasContent());
        Assertions.assertEquals(0, result.getNumber());
        Assertions.assertEquals(10, result.getSize());
        Assertions.assertEquals(COUNT_TOTAL_PRODUCTS, result.getTotalElements());
    }

    @Test
    public void findAllPagedShouldReturnEmptyPageWhenPageDoesNotExist() {
        PageRequest pageRequest = PageRequest.of(50, 10);

        Page<ProductDTO> result = service.findAllPaged(pageRequest);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void findAllPagedShouldReturnSortedPageWhenPageSortByName() {
        PageRequest pageRequest = PageRequest.of(0, 10,
                Sort.by("name"));

        Page<ProductDTO> result = service.findAllPaged(pageRequest);

        Assertions.assertTrue(result.hasContent());
        Assertions.assertEquals("Macbook Pro", result.getContent().get(0).getName());
        Assertions.assertEquals("PC Gamer", result.getContent().get(1).getName());
        Assertions.assertEquals("PC Gamer Alfa", result.getContent().get(2).getName());
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists() {
        var result = service.findById(EXISTING_ID);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(EXISTING_ID, result.getId());
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(NON_EXISTING_ID);
        });
    }

    @Test
    public void insertShouldCreateProductDTO() {
        ProductDTO newProduct = service.insert(Factory.createProductDTO());
        ProductDTO productFound = service.findById(newProduct.getId());

        Assertions.assertEquals(productFound.getName(), newProduct.getName());
        Assertions.assertEquals(productFound.getDescription(), newProduct.getDescription());
        Assertions.assertEquals(productFound.getPrice(), newProduct.getPrice());
        Assertions.assertEquals(productFound.getDate(), newProduct.getDate());
        Assertions.assertEquals(productFound.getImgUrl(), newProduct.getImgUrl());
    }

    @Test
    public void updateShouldUpdateProductDTO() {
        ProductDTO updatedProduct = service.update(EXISTING_ID, Factory.createProductDTO());
        ProductDTO productFound = service.findById(updatedProduct.getId());

        Assertions.assertEquals(productFound.getName(), updatedProduct.getName());
        Assertions.assertEquals(productFound.getDescription(), updatedProduct.getDescription());
        Assertions.assertEquals(productFound.getPrice(), updatedProduct.getPrice());
        Assertions.assertEquals(productFound.getDate(), updatedProduct.getDate());
        Assertions.assertEquals(productFound.getImgUrl(), updatedProduct.getImgUrl());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.update(NON_EXISTING_ID, Factory.createProductDTO()));
    }

    @Test
    public void deleteShouldDeleteResourceWhenIdExist() {
        service.delete(EXISTING_ID);
        Assertions.assertEquals(COUNT_TOTAL_PRODUCTS - 1, repository.count());
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.delete(NON_EXISTING_ID));
    }
}
