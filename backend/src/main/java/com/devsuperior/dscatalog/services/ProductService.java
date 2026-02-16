package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.exceptions.DataBaseException;
import com.devsuperior.dscatalog.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(Pageable pageRequest) {
        Page<Product> categories = repository.findAll(pageRequest);
        return categories.map(ProductDTO::new);
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Optional<Product> Product = repository.findById(id);
        Product entity = Product.orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found!"));
        return new ProductDTO(entity, entity.getCategories());
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product Product = new Product();
        copyDtoToEntity(dto, Product);
        return new ProductDTO(repository.save(Product));
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {
            Product Product = repository.getReferenceById(id);
            copyDtoToEntity(dto, Product);
            return new ProductDTO(repository.save(Product));
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id not found: " + id);
        }
    }

    public void delete(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Id not found: " + id));

        try {
            repository.delete(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DataBaseException("Integrity violation");
        }
    }

    private void copyDtoToEntity(ProductDTO dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setDate(dto.getDate());
        product.setImgUrl(dto.getImgUrl());
        product.setPrice(dto.getPrice());

        product.getCategories().clear();
        for (CategoryDTO categoryDTO : dto.getCategories()) {
            Category category = categoryRepository.getReferenceById(categoryDTO.getId());
            product.getCategories().add(category);
        }
    }

}
