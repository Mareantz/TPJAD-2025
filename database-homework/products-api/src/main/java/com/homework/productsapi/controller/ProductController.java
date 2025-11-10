package com.homework.productsapi.controller;

import com.homework.productsapi.product.Product;
import com.homework.productsapi.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository repository;

    // READ (All)
    @GetMapping
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    // CREATE
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return repository.save(product);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        repository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return repository.findById(id).orElse(null); // or throw 404
    }

    // UPDATE
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product updatedProduct) {
        return repository.findById(id)
                .map(product -> {
                    product.setProductName(updatedProduct.getProductName());
                    product.setPrice(updatedProduct.getPrice());
                    return repository.save(product);
                })
                .orElse(null); // or throw 404
    }
}