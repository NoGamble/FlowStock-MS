package com.flowstock.ms.repository;

import com.flowstock.ms.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.flowstock.ms.entity.OutboundRecord;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

}