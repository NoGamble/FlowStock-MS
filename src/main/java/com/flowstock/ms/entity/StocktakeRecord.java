package com.flowstock.ms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocktake_record")
@Data
public class StocktakeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Inventory inventory;

    @Column(name = "system_quantity")
    private int systemQuantity;

    @Column(name = "actual_quantity")
    private int actualQuantity;

    @Column(name = "diff_quantity", insertable = false, updatable = false)
    private int diffQuantity;

    @Column(name = "stocktake_time")
    private LocalDateTime stocktakeTime = LocalDateTime.now();

}
