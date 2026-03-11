package com.flowstock.ms.controller;

import com.flowstock.ms.repository.*;
import com.flowstock.ms.service.*;
import com.flowstock.ms.entity.*;

import com.flowstock.ms.dto.MovementRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping
    public List<Inventory> list() {
        return productService.getAllProducts();
    }

    @PostMapping
    public Inventory create(@RequestBody Inventory product){
        return productService.createProduct(product);
    }

    @PutMapping("/{id}")
    public Inventory update(@PathVariable Long id, @RequestBody Inventory details){
        return productService.updateProduct(id, details);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        productService.deleteProduct(id);
    }

}
