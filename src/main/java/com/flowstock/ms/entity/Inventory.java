package com.flowstock.ms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inventory")
@Data

public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "current_quantity")
    private Integer currentQuantity;

    @Column(name = "sku_code", unique = true)
    private String skuCode;

    @Column(name = "unit")
    private String unit;

    @Version
    private Integer version;
}