package com.stocktrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private Long totalUsers;
    private Long totalStocks;
    private Long totalTransactions;
    private BigDecimal totalTradingVolume;
    private Long totalActiveUsers;
    private Long totalBuyTransactions;
    private Long totalSellTransactions;
    private List<TradeResponse> recentTransactions;
    private List<UserDto> recentUsers;
}
