package com.flowstock.ms.controller;

import com.flowstock.ms.dto.MovementRequest;
import com.flowstock.ms.entity.Inventory;
import com.flowstock.ms.service.ProductService;
import com.flowstock.ms.service.StockMovementService;
import com.flowstock.ms.service.StocktakeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService movementService;

    public StockMovementController(StockMovementService movementService){
        this.movementService = movementService;
    }

    @PostMapping("/inbound")
    public String inbound(@RequestBody MovementRequest request){
        movementService.processInbound(request.getItemId(), request.getAmount());
        return "Inbound processed successfully!";
    }
    @PutMapping("/inbound/{recordId}")
    public String updateInbound(@PathVariable Long recordId, @RequestBody Integer newAmount) {
        movementService.updateInboundRecord(recordId, newAmount);
        return "Inbound record updated and stock adjusted!";
    }

    @PostMapping("/outbound")
    public String outbound(@RequestBody MovementRequest request){
        movementService.processOutbound(request.getItemId(), request.getAmount());
        return "Outbound processed successfully!";
    }

    @DeleteMapping("/outbound/{recordId}")
    public String cancelOutbound(@PathVariable Long recordId){
        movementService.deleteOutboundRecord(recordId);
        return "Outbound Record cancelled and stock rolled back!";
    }
}
