package com.flowstock.ms;

import  jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ManyToAny;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbound_record")
@Data

public class OutboundRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Inventory inventory;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "outbound_time")
    private LocalDateTime outboundTime = LocalDateTime.now();
}
