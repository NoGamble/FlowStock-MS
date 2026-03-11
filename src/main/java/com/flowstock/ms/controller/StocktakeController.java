package com.flowstock.ms.controller;

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
    public String execute(@RequestParam Long itemId, @RequestParam Integer actualQuantity){
        stocktakeService.executeStocktake(itemId, actualQuantity);
        return "Stocktake completed. Inventory has been synchronized with physical count.";
    }

    @GetMapping
    public List<StocktakeRecord> list(){
        return stocktakeService.getAllStocktakeRecords();
    }

    @DeleteMapping("/{recordId}")
    public String delete(@PathVariable Long recordId){
        stocktakeService.deleteStocktakeRecord(recordId);
        return "Stocktake record #" + recordId + " has been removed from history.";
    }
}
