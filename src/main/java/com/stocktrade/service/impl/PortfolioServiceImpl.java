package com.stocktrade.service.impl;

import com.stocktrade.dto.PortfolioItemDto;
import com.stocktrade.dto.PortfolioSummaryDto;
import com.stocktrade.entity.Portfolio;
import com.stocktrade.entity.Stock;
import com.stocktrade.entity.User;
import com.stocktrade.exception.UserNotFoundException;
import com.stocktrade.repository.PortfolioRepository;
import com.stocktrade.repository.UserRepository;
import com.stocktrade.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioItemDto> getUserPortfolio(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        List<Portfolio> portfolios = portfolioRepository.findByUserId(user.getId());
        List<PortfolioItemDto> items = new ArrayList<>();

        for (Portfolio p : portfolios) {
            items.add(mapToPortfolioItemDto(p));
        }

        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioSummaryDto getUserPortfolioSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        List<Portfolio> portfolios = portfolioRepository.findByUserId(user.getId());
        List<PortfolioItemDto> items = new ArrayList<>();

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal todayProfitLoss = BigDecimal.ZERO;

        for (Portfolio p : portfolios) {
            PortfolioItemDto item = mapToPortfolioItemDto(p);
            items.add(item);

            totalPortfolioValue = totalPortfolioValue.add(item.getCurrentValue());
            totalInvestment = totalInvestment.add(item.getInvestedAmount());

            Stock stock = p.getStock();
            if (stock.getPreviousClose() != null) {
                BigDecimal dayDelta = stock.getCurrentPrice().subtract(stock.getPreviousClose());
                BigDecimal stockTodayPL = dayDelta.multiply(BigDecimal.valueOf(p.getQuantity()));
                todayProfitLoss = todayProfitLoss.add(stockTodayPL);
            }
        }

        BigDecimal totalProfitLoss = totalPortfolioValue.subtract(totalInvestment).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalProfitLossPercentage = BigDecimal.ZERO;

        if (totalInvestment.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossPercentage = totalProfitLoss.divide(totalInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        return PortfolioSummaryDto.builder()
                .userId(user.getId())
                .walletBalance(user.getWalletBalance())
                .totalPortfolioValue(totalPortfolioValue.setScale(2, RoundingMode.HALF_UP))
                .totalInvestment(totalInvestment.setScale(2, RoundingMode.HALF_UP))
                .totalProfitLoss(totalProfitLoss)
                .totalProfitLossPercentage(totalProfitLossPercentage)
                .todayProfitLoss(todayProfitLoss.setScale(2, RoundingMode.HALF_UP))
                .numberOfStocksOwned(items.size())
                .holdings(items)
                .build();
    }

    private PortfolioItemDto mapToPortfolioItemDto(Portfolio p) {
        Stock stock = p.getStock();
        int quantity = p.getQuantity();
        BigDecimal avgBuyPrice = p.getAverageBuyPrice();
        BigDecimal currentPrice = stock.getCurrentPrice();

        BigDecimal investedAmount = avgBuyPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentValue = currentPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal profitLoss = currentValue.subtract(investedAmount).setScale(2, RoundingMode.HALF_UP);

        BigDecimal profitLossPercentage = BigDecimal.ZERO;
        if (investedAmount.compareTo(BigDecimal.ZERO) > 0) {
            profitLossPercentage = profitLoss.divide(investedAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal priceChange = BigDecimal.ZERO;
        BigDecimal percentageChange = BigDecimal.ZERO;
        if (stock.getPreviousClose() != null && stock.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
            priceChange = currentPrice.subtract(stock.getPreviousClose()).setScale(2, RoundingMode.HALF_UP);
            percentageChange = priceChange.divide(stock.getPreviousClose(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        return PortfolioItemDto.builder()
                .id(p.getId())
                .stockId(stock.getId())
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .sector(stock.getSector())
                .quantity(quantity)
                .averageBuyPrice(avgBuyPrice)
                .currentPrice(currentPrice)
                .investedAmount(investedAmount)
                .currentValue(currentValue)
                .profitLoss(profitLoss)
                .profitLossPercentage(profitLossPercentage)
                .priceChange(priceChange)
                .percentageChange(percentageChange)
                .build();
    }
}
