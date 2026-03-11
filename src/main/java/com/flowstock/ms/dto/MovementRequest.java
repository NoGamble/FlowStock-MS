package com.flowstock.ms.dto;

public class MovementRequest {
    private Long itemId;
    private Integer amount;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId){ this.itemId = itemId;}
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

}
