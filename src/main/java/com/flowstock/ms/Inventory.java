package com.flowstock.ms;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inventory")
@Data

public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name")
    private String itemName;

    private Integer quantity;
}