package com.flowstock.ms.service;

import com.flowstock.ms.entity.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.flowstock.ms.repository.*;
import java.util.List;

@Service
public class StockMovementService {
    private final InventoryRepository inventoryRepository;
    private final InboundRecordRepository inboundRecordRepository;
    private final OutboundRecordRepository outboundRecordRepository;

    public StockMovementService(InventoryRepository inventoryRepository,
                                InboundRecordRepository inboundRecordRepository,
                                OutboundRecordRepository outboundRecordRepository){
        this.inventoryRepository = inventoryRepository;
        this.inboundRecordRepository = inboundRecordRepository;
        this.outboundRecordRepository = outboundRecordRepository;
    }

    /**
     * Core Logic: Process a new inbound shipment.
     * Updates inventory levels and creates a record.
     * @param itemId the ID of the product to inbound
     * @param amount the amount of the inbound item
     */
    @Transactional
    public void processInbound(Long itemId, Integer amount){
        Inventory item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        item.setCurrentQuantity(item.getCurrentQuantity() + amount);
        inventoryRepository.save(item);

        InboundRecord record = new InboundRecord();
        record.setInventory(item);
        record.setQuantity(amount);
        inboundRecordRepository.save(record);
    }

    /**
     * Update an existing inbound record.
     * Adjusts the inventory by calculating the difference between old and new amounts.
     * @param recordId the ID of the record you wanna update
     * @param newAmount the new amount to update the record
     */
    @Transactional
    public void updateInboundRecord(Long recordId, Integer newAmount) {
        InboundRecord record = inboundRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Inbound record not found"));

        Inventory item = record.getInventory();
        int difference = newAmount - record.getQuantity();

        // Update Inventory and then the Record
        item.setCurrentQuantity(item.getCurrentQuantity() + difference);
        inventoryRepository.save(item);

        record.setQuantity(newAmount);
        inboundRecordRepository.save(record);
    }

    /**
     * Core Logic: Get all inbound records
     */
    public List<InboundRecord> getAllInboundRecords() {
        return inboundRecordRepository.findAll();
    }

    /**
     * Core Logic: Process a new outbound shipment.
     * Updates inventory levels and creates a record.
     * @param itemId the ID of the product to outbound
     * @param amount the amount of the outbound item
     */
    @Transactional
    public void processOutbound(Long itemId, Integer amount){
        Inventory item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        if (item.getCurrentQuantity() < amount) {
            throw new RuntimeException("Insufficient stock for: " + item.getItemName());
        }

        item.setCurrentQuantity(item.getCurrentQuantity() - amount);
        inventoryRepository.save(item);

        OutboundRecord record = new OutboundRecord();
        record.setInventory(item);
        record.setQuantity(amount);
        outboundRecordRepository.save(record);
    }

    /**
     * Cancel/Delete an outbound record and roll back the stock.
     * @param recordId the ID of the record you wanna delete
     */
    @Transactional
    public void deleteOutboundRecord(Long recordId) {
        OutboundRecord record = outboundRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Outbound record not found"));

        Inventory item = record.getInventory();

        item.setCurrentQuantity(item.getCurrentQuantity() + record.getQuantity());
        inventoryRepository.save(item);

        outboundRecordRepository.delete(record);
    }

    /**
     * Core Logic: Get all outbound records
     */
    public List<OutboundRecord> getAllOutboundRecords() {
        return outboundRecordRepository.findAll();
    }
}
