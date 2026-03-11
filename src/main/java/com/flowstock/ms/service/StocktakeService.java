package com.flowstock.ms.service;

import com.flowstock.ms.entity.*;
import com.flowstock.ms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StocktakeService {

    private final InventoryRepository inventoryRepository;
    private final StocktakeRecordRepository stocktakeRecordRepository;

    public StocktakeService(InventoryRepository inventoryRepository,
                            StocktakeRecordRepository stocktakeRecordRepository) {
        this.inventoryRepository = inventoryRepository;
        this.stocktakeRecordRepository = stocktakeRecordRepository;
    }

    /**
     * Core Logic: Execute a stocktake process.
     * Calculates the difference between system and actual quantity,
     * then forces the inventory to match reality.
     * * @param itemId the ID of the product being counted
     * @param actualQuantity the physical count results from the warehouse
     */
    @Transactional
    public void executeStocktake(Long itemId, Integer actualQuantity) {
        Inventory item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        Integer systemQuantity = item.getCurrentQuantity();
        Integer difference = actualQuantity - systemQuantity;

        StocktakeRecord record = new StocktakeRecord();
        record.setInventory(item);
        record.setSystemQuantity(systemQuantity);
        record.setActualQuantity(actualQuantity);
        record.setDiffQuantity(difference);

        stocktakeRecordRepository.save(record);

        item.setCurrentQuantity(actualQuantity);
        inventoryRepository.save(item);
    }

    /**
     * Retrieve all stocktake history for auditing.
     * @return List of all stocktake records.
     */
    public List<StocktakeRecord> getAllStocktakeRecords() {
        return stocktakeRecordRepository.findAll();
    }

    /**
     * Delete a stocktake record.
     * Note: Usually, stocktake records are kept for audit and not deleted.
     * @param recordId ID of the record to remove.
     */
    public void deleteStocktakeRecord(Long recordId) {
        stocktakeRecordRepository.deleteById(recordId);
    }
}