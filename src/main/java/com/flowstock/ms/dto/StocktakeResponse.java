package com.flowstock.ms.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StocktakeResponse {
    private Long id;
    private Integer systemQuantity;
    private Integer actualQuantity;
    private Integer diffQuantity;
    private LocalDateTime stocktakeTime;

    private Long itemId;
    private String itemName;
}