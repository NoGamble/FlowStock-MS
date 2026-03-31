package com.flowstock.ms.controller;

import com.flowstock.ms.dto.Result;
import com.flowstock.ms.entity.Inventory;
import com.flowstock.ms.repository.InventoryRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
//@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    public InventoryController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping
    public Result<List<Inventory>> getAllInventory() {
        List<Inventory> list = inventoryRepository.findAll();
        return Result.success(list);
    }
}