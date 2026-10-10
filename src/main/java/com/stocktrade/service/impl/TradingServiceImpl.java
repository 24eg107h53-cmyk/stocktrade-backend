package com.stocktrade.service.impl;

import com.stocktrade.dto.BuyRequest;
import com.stocktrade.dto.SellRequest;
import com.stocktrade.dto.TradeResponse;
import com.stocktrade.entity.*;
import com.stocktrade.exception.InsufficientBalanceException;
import com.stocktrade.exception.InsufficientStockException;
import com.stocktrade.exception.InvalidTransactionException;
import com.stocktrade.exception.StockNotFoundException;
import com.stocktrade.exception.UserNotFoundException;
import com.stocktrade.repository.PortfolioRepository;
import com.stocktrade.repository.StockRepository;
import com.stocktrade.repository.TransactionRepository;
import com.stocktrade.repository.UserRepository;
import com.stocktrade.service.TradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradingServiceImpl implements TradingService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public TradeResponse buyStock(BuyRequest buyRequest, String authenticatedUsername) {
        User user = resolveUser(buyRequest.getUserId(), authenticatedUsername);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidTransactionException("Account is inactive. Trading operations are disabled.");
        }

        if (buyRequest.getQuantity() == null || buyRequest.getQuantity() <= 0) {
            throw new InvalidTransactionException("Buy quantity must be greater than zero.");
        }

        Stock stock = stockRepository.findById(buyRequest.getStockId())
                .orElseThrow(() -> new StockNotFoundException("Stock not found with ID: " + buyRequest.getStockId()));

        BigDecimal currentPrice = stock.getCurrentPrice();
        BigDecimal totalAmount = currentPrice.multiply(BigDecimal.valueOf(buyRequest.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        if (user.getWalletBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient wallet balance. Total required: ₹%s, Available: ₹%s",
                            totalAmount, user.getWalletBalance())
            );
        }

        // Deduct from wallet
        user.setWalletBalance(user.getWalletBalance().subtract(totalAmount));
        userRepository.save(user);

        // Update or create portfolio entry
        Optional<Portfolio> existingOpt = portfolioRepository.findByUserIdAndStockId(user.getId(), stock.getId());
        int finalQuantity;
        if (existingOpt.isPresent()) {
            Portfolio existing = existingOpt.get();
            int oldQuantity = existing.getQuantity();
            BigDecimal oldTotalCost = existing.getAverageBuyPrice().multiply(BigDecimal.valueOf(oldQuantity));
            BigDecimal newTotalCost = oldTotalCost.add(totalAmount);
            finalQuantity = oldQuantity + buyRequest.getQuantity();

            BigDecimal newAverageBuyPrice = newTotalCost.divide(BigDecimal.valueOf(finalQuantity), 2, RoundingMode.HALF_UP);
            existing.setQuantity(finalQuantity);
            existing.setAverageBuyPrice(newAverageBuyPrice);
            portfolioRepository.save(existing);
        } else {
            finalQuantity = buyRequest.getQuantity();
            Portfolio newPortfolio = Portfolio.builder()
                    .user(user)
                    .stock(stock)
                    .quantity(finalQuantity)
                    .averageBuyPrice(currentPrice)
                    .build();
            portfolioRepository.save(newPortfolio);
        }

        // Record BUY transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .stock(stock)
                .transactionType(TransactionType.BUY)
                .quantity(buyRequest.getQuantity())
                .price(currentPrice)
                .totalAmount(totalAmount)
                .transactionDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();

        Transaction savedTx = transactionRepository.save(transaction);

        return TradeResponse.builder()
                .transactionId(savedTx.getId())
                .userId(user.getId())
                .stockId(stock.getId())
                .stockSymbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .transactionType(TransactionType.BUY)
                .quantity(buyRequest.getQuantity())
                .price(currentPrice)
                .totalAmount(totalAmount)
                .transactionDate(savedTx.getTransactionDate())
                .status("COMPLETED")
                .message(String.format("Successfully bought %d shares of %s at ₹%s each",
                        buyRequest.getQuantity(), stock.getSymbol(), currentPrice))
                .remainingWalletBalance(user.getWalletBalance())
                .remainingStockQuantity(finalQuantity)
                .build();
    }

    @Override
    @Transactional
    public TradeResponse sellStock(SellRequest sellRequest, String authenticatedUsername) {
        User user = resolveUser(sellRequest.getUserId(), authenticatedUsername);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidTransactionException("Account is inactive. Trading operations are disabled.");
        }

        if (sellRequest.getQuantity() == null || sellRequest.getQuantity() <= 0) {
            throw new InvalidTransactionException("Sell quantity must be greater than zero.");
        }

        Stock stock = stockRepository.findById(sellRequest.getStockId())
                .orElseThrow(() -> new StockNotFoundException("Stock not found with ID: " + sellRequest.getStockId()));

        Portfolio portfolio = portfolioRepository.findByUserIdAndStockId(user.getId(), stock.getId())
                .orElseThrow(() -> new InsufficientStockException("You do not own any shares of " + stock.getSymbol()));

        if (portfolio.getQuantity() < sellRequest.getQuantity()) {
            throw new InsufficientStockException(
                    String.format("Insufficient shares. You own %d shares of %s, but requested to sell %d",
                            portfolio.getQuantity(), stock.getSymbol(), sellRequest.getQuantity())
            );
        }

        BigDecimal currentPrice = stock.getCurrentPrice();
        BigDecimal totalAmount = currentPrice.multiply(BigDecimal.valueOf(sellRequest.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        // Update portfolio
        int remainingQuantity = portfolio.getQuantity() - sellRequest.getQuantity();
        if (remainingQuantity == 0) {
            portfolioRepository.delete(portfolio);
        } else {
            portfolio.setQuantity(remainingQuantity);
            portfolioRepository.save(portfolio);
        }

        // Add to wallet
        user.setWalletBalance(user.getWalletBalance().add(totalAmount));
        userRepository.save(user);

        // Record SELL transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .stock(stock)
                .transactionType(TransactionType.SELL)
                .quantity(sellRequest.getQuantity())
                .price(currentPrice)
                .totalAmount(totalAmount)
                .transactionDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();

        Transaction savedTx = transactionRepository.save(transaction);

        return TradeResponse.builder()
                .transactionId(savedTx.getId())
                .userId(user.getId())
                .stockId(stock.getId())
                .stockSymbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .transactionType(TransactionType.SELL)
                .quantity(sellRequest.getQuantity())
                .price(currentPrice)
                .totalAmount(totalAmount)
                .transactionDate(savedTx.getTransactionDate())
                .status("COMPLETED")
                .message(String.format("Successfully sold %d shares of %s for ₹%s",
                        sellRequest.getQuantity(), stock.getSymbol(), totalAmount))
                .remainingWalletBalance(user.getWalletBalance())
                .remainingStockQuantity(remainingQuantity)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getUserTrades(Long userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId).stream()
                .map(this::mapToTradeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getAllTrades() {
        return transactionRepository.findAllByOrderByTransactionDateDesc().stream()
                .map(this::mapToTradeResponse)
                .collect(Collectors.toList());
    }

    private User resolveUser(Long requestedUserId, String authenticatedUsername) {
        if (authenticatedUsername != null && !authenticatedUsername.isEmpty()) {
            return userRepository.findByUsernameOrEmail(authenticatedUsername, authenticatedUsername)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + authenticatedUsername));
        }
        if (requestedUserId != null) {
            return userRepository.findById(requestedUserId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + requestedUserId));
        }
        throw new InvalidTransactionException("User identification is missing.");
    }

    private TradeResponse mapToTradeResponse(Transaction tx) {
        return TradeResponse.builder()
                .transactionId(tx.getId())
                .userId(tx.getUser() != null ? tx.getUser().getId() : null)
                .stockId(tx.getStock() != null ? tx.getStock().getId() : null)
                .stockSymbol(tx.getStock() != null ? tx.getStock().getSymbol() : "N/A")
                .companyName(tx.getStock() != null ? tx.getStock().getCompanyName() : "N/A")
                .transactionType(tx.getTransactionType())
                .quantity(tx.getQuantity())
                .price(tx.getPrice())
                .totalAmount(tx.getTotalAmount())
                .transactionDate(tx.getTransactionDate())
                .status(tx.getStatus())
                .message("Trade executed successfully")
                .build();
    }
}
