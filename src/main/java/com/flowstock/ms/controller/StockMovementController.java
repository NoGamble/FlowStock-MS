package com.flowstock.ms.controller;

import com.flowstock.ms.dto.MovementRequest;
import com.flowstock.ms.dto.Result;
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
    public Result<Void> inbound(@RequestBody MovementRequest request){
        movementService.processInbound(request.getItemId(), request.getAmount());
        // 返回统一的成功状态，信息已经在 Result 类里默认设为“操作成功”
        return Result.success();
    }

    @PutMapping("/inbound/{recordId}")
    public Result<Void> updateInbound(@PathVariable Long recordId, @RequestBody Integer newAmount) {
        movementService.updateInboundRecord(recordId, newAmount);
        return Result.success();
    }

    @PostMapping("/outbound")
    public Result<Void> outbound(@RequestBody MovementRequest request){
        movementService.processOutbound(request.getItemId(), request.getAmount());
        return Result.success();
    }

    @DeleteMapping("/outbound/{recordId}")
    public Result<Void> cancelOutbound(@PathVariable Long recordId){
        movementService.deleteOutboundRecord(recordId);
        return Result.success();
    }
}
