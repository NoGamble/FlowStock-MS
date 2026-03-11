package com.flowstock.ms.service;

import com.flowstock.ms.entity.Inventory;
import com.flowstock.ms.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductService{
    private final InventoryRepository inventoryRepository;
    public ProductService(InventoryRepository inventoryRepository){
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Retrieve all products from the inventory.
     * @return List of all inventory items.
     */
    public List<Inventory> getAllProducts(){
        return inventoryRepository.findAll();
    }

    /**
     * Create and save a new product into the database.
     * @param product The product entity to be created.
     * @return The saved product entity with generated ID.
     */
    public Inventory createProduct(Inventory product){
        return inventoryRepository.save(product);
    }

    /**
     * Remove a product from the database.
     * @param id The ID of the product to be deleted.
     */
    public void deleteProduct(Long id){
        inventoryRepository.deleteById(id);
    }

    /**
     * Update an existing product's information.
     * @param id The ID of the product to update.
     * @param details The new details to apply.
     * @return The updated product.
     */
    public Inventory updateProduct(Long id, Inventory details) {
        Inventory existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        existing.setItemName(details.getItemName());
        return inventoryRepository.save(existing);
    }


}