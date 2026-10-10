package com.stocktrade.service.impl;

import com.stocktrade.dto.AdminDashboardDto;
import com.stocktrade.dto.PortfolioSummaryDto;
import com.stocktrade.dto.TradeResponse;
import com.stocktrade.dto.UserDto;
import com.stocktrade.entity.Transaction;
import com.stocktrade.entity.TransactionType;
import com.stocktrade.entity.User;
import com.stocktrade.entity.UserStatus;
import com.stocktrade.exception.UserNotFoundException;
import com.stocktrade.repository.StockRepository;
import com.stocktrade.repository.TransactionRepository;
import com.stocktrade.repository.UserRepository;
import com.stocktrade.service.AdminService;
import com.stocktrade.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final TransactionRepository transactionRepository;
    private final PortfolioService portfolioService;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setStatus(status);
        User updated = userRepository.save(user);
        return mapToUserDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDesc().stream()
                .map(this::mapToTradeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalStocks = stockRepository.count();
        long totalTransactions = transactionRepository.count();
        BigDecimal totalTradingVolume = transactionRepository.calculateTotalTradingVolume();
        if (totalTradingVolume == null) {
            totalTradingVolume = BigDecimal.ZERO;
        }

        List<User> allUsers = userRepository.findAll();
        long totalActiveUsers = allUsers.stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .count();

        long totalBuyTransactions = transactionRepository.countByTransactionType(TransactionType.BUY);
        long totalSellTransactions = transactionRepository.countByTransactionType(TransactionType.SELL);

        List<TradeResponse> recentTransactions = transactionRepository.findAllByOrderByTransactionDateDesc().stream()
                .limit(10)
                .map(this::mapToTradeResponse)
                .collect(Collectors.toList());

        List<UserDto> recentUsers = allUsers.stream()
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .limit(5)
                .map(this::mapToUserDto)
                .collect(Collectors.toList());

        return AdminDashboardDto.builder()
                .totalUsers(totalUsers)
                .totalStocks(totalStocks)
                .totalTransactions(totalTransactions)
                .totalTradingVolume(totalTradingVolume)
                .totalActiveUsers(totalActiveUsers)
                .totalBuyTransactions(totalBuyTransactions)
                .totalSellTransactions(totalSellTransactions)
                .recentTransactions(recentTransactions)
                .recentUsers(recentUsers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioSummaryDto getUserPortfolio(Long userId) {
        return portfolioService.getUserPortfolioSummary(userId);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .walletBalance(user.getWalletBalance())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
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
                .message("Completed")
                .build();
    }
}
