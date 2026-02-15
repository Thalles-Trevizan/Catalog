package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.exceptions.ControllerNotFoundException;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository repository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        List<Category> categories = repository.findAll();
        return categories.stream().map(CategoryDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Optional<Category> category = repository.findById(id);
        Category entity = category.orElseThrow(() -> new ControllerNotFoundException("Category with id " + id + " not found!"));
        return new CategoryDTO(entity);
    }

    @Transactional
    public CategoryDTO insert(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return new CategoryDTO(repository.save(category));
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        try {
            Category category = repository.getOne(id);
            category.setName(dto.getName());
            return new CategoryDTO(repository.save(category));
        } catch (EntityNotFoundException e) {
            throw new ControllerNotFoundException("Id not found: " + id);
        }
    }
}
