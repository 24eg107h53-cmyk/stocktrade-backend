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
public class PortfolioSummaryDto {
    private Long userId;
    private BigDecimal walletBalance;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalInvestment;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercentage;
    private BigDecimal todayProfitLoss;
    private Integer numberOfStocksOwned;
    private List<PortfolioItemDto> holdings;
}
