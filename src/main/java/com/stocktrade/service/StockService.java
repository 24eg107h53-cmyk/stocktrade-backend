package com.stocktrade.service;

import com.stocktrade.dto.StockDto;

import java.math.BigDecimal;
import java.util.List;

public interface StockService {
    List<StockDto> getAllStocks();
    StockDto getStockById(Long id);
    StockDto getStockBySymbol(String symbol);
    StockDto createStock(StockDto stockDto);
    StockDto updateStock(Long id, StockDto stockDto);
    void deleteStock(Long id);
    StockDto updateStockPrice(Long id, BigDecimal newPrice);
    List<StockDto> searchStocks(String query);
    List<StockDto> simulateMarketTick();
}
