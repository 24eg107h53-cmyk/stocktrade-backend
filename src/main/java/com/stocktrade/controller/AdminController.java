package com.stocktrade.controller;

import com.stocktrade.dto.AdminDashboardDto;
import com.stocktrade.dto.PortfolioSummaryDto;
import com.stocktrade.dto.TradeResponse;
import com.stocktrade.dto.UserDto;
import com.stocktrade.dto.UserStatusUpdateDto;
import com.stocktrade.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable Long id,
                                                    @Valid @RequestBody UserStatusUpdateDto statusDto) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, statusDto.getStatus()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TradeResponse>> getAllTransactions() {
        return ResponseEntity.ok(adminService.getAllTransactions());
    }

    @GetMapping("/users/{id}/portfolio")
    public ResponseEntity<PortfolioSummaryDto> getUserPortfolio(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserPortfolio(id));
    }
}
