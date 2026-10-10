package com.stocktrade.controller;

import com.stocktrade.dto.StockDto;
import com.stocktrade.dto.StockPriceUpdateDto;
import com.stocktrade.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockDto>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockDto> getStockById(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getStockById(id));
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<StockDto> getStockBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockBySymbol(symbol));
    }

    @GetMapping("/search")
    public ResponseEntity<List<StockDto>> searchStocks(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(stockService.searchStocks(query));
    }

    @PostMapping
    public ResponseEntity<StockDto> createStock(@Valid @RequestBody StockDto stockDto) {
        StockDto created = stockService.createStock(stockDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockDto> updateStock(@PathVariable Long id, @Valid @RequestBody StockDto stockDto) {
        StockDto updated = stockService.updateStock(id, stockDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<StockDto> updateStockPrice(@PathVariable Long id, @Valid @RequestBody StockPriceUpdateDto updateDto) {
        StockDto updated = stockService.updateStockPrice(id, updateDto.getPrice());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/simulate-tick")
    public ResponseEntity<List<StockDto>> simulateMarketTick() {
        return ResponseEntity.ok(stockService.simulateMarketTick());
    }
}
