package com.devsuperior.dscatalog.services;


import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.factories.Factory;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class CategoryServiceIT {

    @Autowired
    private CategoryService service;

    @Autowired
    private CategoryRepository repository;

    private final long EXISTING_ID = 1L;
    private final long NON_EXISTING_ID = 999L;
    private long COUNT_TOTAL_CATEGORIES = 3L;

    @Test
    public void findAllPagedShouldReturnPageWhenPage0Size10() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<CategoryDTO> result = service.findAllPaged(pageRequest);

        Assertions.assertTrue(result.hasContent());
        Assertions.assertEquals(0, result.getNumber());
        Assertions.assertEquals(10, result.getSize());
        Assertions.assertEquals(COUNT_TOTAL_CATEGORIES, result.getTotalElements());
    }

    @Test
    public void findAllPagedShouldReturnEmptyPageWhenPageDoesNotExist() {
        PageRequest pageRequest = PageRequest.of(50, 10);

        Page<CategoryDTO> result = service.findAllPaged(pageRequest);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void findAllPagedShouldReturnSortedPageWhenPageSortByName() {
        PageRequest pageRequest = PageRequest.of(0, 10,
                Sort.by("name"));

        Page<CategoryDTO> result = service.findAllPaged(pageRequest);

        Assertions.assertTrue(result.hasContent());
        Assertions.assertEquals("Books", result.getContent().get(0).getName());
        Assertions.assertEquals("Computers", result.getContent().get(1).getName());
        Assertions.assertEquals("Eletronics", result.getContent().get(2).getName());
    }

    @Test
    public void findByIdShouldReturnCategoryDTOWhenIdExists() {
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
    public void insertShouldCreateCategoryDTO() {
        CategoryDTO newCategory = service.insert(Factory.createCategoryDTO());
        CategoryDTO categoryFound = service.findById(newCategory.getId());

        Assertions.assertEquals(categoryFound.getName(), newCategory.getName());
    }

    @Test
    public void updateShouldUpdateCategoryDTO() {
        CategoryDTO updatedCategory = service.update(EXISTING_ID, Factory.createCategoryDTO());
        CategoryDTO categoryFound = service.findById(updatedCategory.getId());

        Assertions.assertEquals(categoryFound.getName(), updatedCategory.getName());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.update(NON_EXISTING_ID, Factory.createCategoryDTO()));
    }

    @Test
    public void deleteShouldDeleteResourceWhenIdExist() {
        CategoryDTO newCategory = service.insert(Factory.createCategoryDTO());
        service.delete(newCategory.getId());
        Assertions.assertEquals(COUNT_TOTAL_CATEGORIES, repository.count());
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.delete(NON_EXISTING_ID));
    }

    @Test
    public void deleteShouldThrowDataIntegrityViolationExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> {
                    service.delete(EXISTING_ID);
                    repository.flush();
                });
    }
}
