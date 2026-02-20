package com.devsuperior.dscatalog.factories;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;

import static java.time.Instant.now;

public class Factory {

    public static Product createProduct() {
        Product product = new Product(1L, "Iphone", "Godd phone", 800D, "https://img.com", now());
        product.getCategories().add(createCategory());
        return product;
    }

    public static ProductDTO createProductDTO() {
        Product product = createProduct();
        return new ProductDTO(product, product.getCategories());
    }

    public static Category createCategory() {
        return new Category(1L, "Eletronics", now(), now());
    }
}
