package com.stocktrade.service;

import com.stocktrade.dto.AdminDashboardDto;
import com.stocktrade.dto.PortfolioSummaryDto;
import com.stocktrade.dto.TradeResponse;
import com.stocktrade.dto.UserDto;
import com.stocktrade.entity.UserStatus;

import java.util.List;

public interface AdminService {
    List<UserDto> getAllUsers();
    UserDto updateUserStatus(Long userId, UserStatus status);
    List<TradeResponse> getAllTransactions();
    AdminDashboardDto getDashboardStats();
    PortfolioSummaryDto getUserPortfolio(Long userId);
}
