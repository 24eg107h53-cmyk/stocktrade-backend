package com.stocktrade.controller;

import com.stocktrade.dto.PortfolioItemDto;
import com.stocktrade.dto.PortfolioSummaryDto;
import com.stocktrade.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<PortfolioItemDto>> getUserPortfolio(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getUserPortfolio(userId));
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<PortfolioSummaryDto> getUserPortfolioSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getUserPortfolioSummary(userId));
    }
}
