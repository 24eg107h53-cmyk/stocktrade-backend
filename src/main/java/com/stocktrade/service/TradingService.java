package com.stocktrade.service;

import com.stocktrade.dto.BuyRequest;
import com.stocktrade.dto.SellRequest;
import com.stocktrade.dto.TradeResponse;

import java.util.List;

public interface TradingService {
    TradeResponse buyStock(BuyRequest buyRequest, String authenticatedUsername);
    TradeResponse sellStock(SellRequest sellRequest, String authenticatedUsername);
    List<TradeResponse> getUserTrades(Long userId);
    List<TradeResponse> getAllTrades();
}
