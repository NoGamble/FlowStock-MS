package com.flowstock.ms.service;

import com.flowstock.ms.entity.*;
import com.flowstock.ms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InboundRecordRepository inboundRecordRepository;
    private final OutboundRecordRepository outboundRecordRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            InboundRecordRepository inboundRecordRepository,
                            OutboundRecordRepository outboundRecordRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inboundRecordRepository = inboundRecordRepository;
        this.outboundRecordRepository = outboundRecordRepository;
    }

    /**
     * Core Business Logic: Process Inbound Stock
     * @param itemId Product ID
     * @param amount Quantity to add
     */
    @Transactional
    public void processInbound(Long itemId, Integer amount) {

        // 1. Find the item or throw an exception if not found
        Inventory item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        // 2. Update the master inventory quantity
        item.setCurrentQuantity(item.getCurrentQuantity() + amount);
        inventoryRepository.save(item);

        // 3. Create and save the inbound transaction record
        InboundRecord record = new InboundRecord();
        record.setInventory(item);
        record.setQuantity(amount);
        inboundRecordRepository.save(record);
    }
    @Transactional
    public void processOutbound(Long itemId, Integer amount) {

        Inventory item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        if (item.getCurrentQuantity() < amount) {
            throw new RuntimeException("Insufficient stock! Item: " + item.getItemName() +
                    ", Available: " + item.getCurrentQuantity() +
                    ", Requested: " + amount);
        }

        item.setCurrentQuantity(item.getCurrentQuantity() - amount);
        inventoryRepository.save(item);

        OutboundRecord record = new OutboundRecord();
        record.setInventory(item);
        record.setQuantity(amount);
        outboundRecordRepository.save(record);
    }
}