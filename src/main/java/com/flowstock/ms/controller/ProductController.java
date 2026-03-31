package com.flowstock.ms.controller;

import com.flowstock.ms.dto.Result;
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
    public Result<List<Inventory>> list() {
        return Result.success(productService.getAllProducts());
    }

    @PostMapping
    public Result<Inventory> create(@RequestBody Inventory product){
        return Result.success(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public Result<Inventory> update(@PathVariable Long id, @RequestBody Inventory details){
        return Result.success(productService.updateProduct(id, details));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        productService.deleteProduct(id);
        return Result.success();
    }

}
