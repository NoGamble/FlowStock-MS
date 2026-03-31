package com.flowstock.ms.controller;

import com.flowstock.ms.dto.Result;
import com.flowstock.ms.dto.StocktakeResponse;
import com.flowstock.ms.entity.StocktakeRecord;
import com.flowstock.ms.service.StocktakeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocktakes")
public class StocktakeController {
    private final StocktakeService stocktakeService;
    public StocktakeController(StocktakeService stocktakeService){
        this.stocktakeService = stocktakeService;
    }

    @PostMapping
    public Result<Void> execute(@RequestParam Long itemId, @RequestParam Integer actualQuantity){
        stocktakeService.executeStocktake(itemId, actualQuantity);
        return Result.success();
    }

    @GetMapping
    public Result<List<StocktakeResponse>> list(){
        List<StocktakeResponse> records = stocktakeService.getAllStocktakeRecords();
        return Result.success(records);
    }

    @DeleteMapping("/{recordId}")
    public Result<Void> delete(@PathVariable Long recordId){
        stocktakeService.deleteStocktakeRecord(recordId);
        return Result.success();
    }
}
