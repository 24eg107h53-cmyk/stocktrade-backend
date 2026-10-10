package com.stocktrade.controller;

import com.stocktrade.dto.BuyRequest;
import com.stocktrade.dto.SellRequest;
import com.stocktrade.dto.TradeResponse;
import com.stocktrade.service.TradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradingController {

    private final TradingService tradingService;

    @PostMapping("/buy")
    public ResponseEntity<TradeResponse> buyStock(@Valid @RequestBody BuyRequest buyRequest,
                                                  Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        TradeResponse response = tradingService.buyStock(buyRequest, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sellStock(@Valid @RequestBody SellRequest sellRequest,
                                                   Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        TradeResponse response = tradingService.sellStock(sellRequest, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TradeResponse>> getUserTrades(@PathVariable Long userId) {
        return ResponseEntity.ok(tradingService.getUserTrades(userId));
    }

    @GetMapping
    public ResponseEntity<List<TradeResponse>> getAllTrades() {
        return ResponseEntity.ok(tradingService.getAllTrades());
    }
}
