package com.stocktrade.service;

import com.stocktrade.dto.PortfolioItemDto;
import com.stocktrade.dto.PortfolioSummaryDto;

import java.util.List;

public interface PortfolioService {
    List<PortfolioItemDto> getUserPortfolio(Long userId);
    PortfolioSummaryDto getUserPortfolioSummary(Long userId);
}
