package com.flowstock.ms.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InboundRecordDTO {
    private Long id;
    private Long itemId;
    private Integer quantity;
    private LocalDateTime inboundTime;
}
