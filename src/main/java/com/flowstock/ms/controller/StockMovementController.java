package com.flowstock.ms.controller;

import com.flowstock.ms.dto.InboundRecordDTO;
import com.flowstock.ms.dto.MovementRequest;
import com.flowstock.ms.dto.OutboundRecordDTO;
import com.flowstock.ms.dto.Result;
import com.flowstock.ms.entity.InboundRecord;
import com.flowstock.ms.entity.OutboundRecord;
import com.flowstock.ms.service.StockMovementService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService movementService;

    public StockMovementController(StockMovementService movementService){
        this.movementService = movementService;
    }

    @GetMapping("/inbound")
    public Result<List<InboundRecordDTO>> listInbound() {
        return Result.success(movementService.getAllInboundRecords().stream()
                .map(r -> { InboundRecordDTO dto = new InboundRecordDTO(); dto.setId(r.getId()); dto.setItemId(r.getInventory().getId()); dto.setQuantity(r.getQuantity()); dto.setInboundTime(r.getInboundTime()); return dto; })
                .collect(Collectors.toList()));
    }

    @GetMapping("/outbound")
    public Result<List<OutboundRecordDTO>> listOutbound() {
        return Result.success(movementService.getAllOutboundRecords().stream()
                .map(r -> { OutboundRecordDTO dto = new OutboundRecordDTO(); dto.setId(r.getId()); dto.setItemId(r.getInventory().getId()); dto.setQuantity(r.getQuantity()); dto.setOutboundTime(r.getOutboundTime()); return dto; })
                .collect(Collectors.toList()));
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
