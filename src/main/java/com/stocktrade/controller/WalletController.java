package com.stocktrade.controller;

import com.stocktrade.dto.WalletRequest;
import com.stocktrade.dto.WalletResponse;
import com.stocktrade.dto.WalletTransactionDto;
import com.stocktrade.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWalletInfo(userId));
    }

    @PostMapping("/deposit")
    public ResponseEntity<WalletResponse> depositMoney(@Valid @RequestBody WalletRequest walletRequest,
                                                       Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(walletService.depositMoney(walletRequest, username));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WalletResponse> withdrawMoney(@Valid @RequestBody WalletRequest walletRequest,
                                                        Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(walletService.withdrawMoney(walletRequest, username));
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<WalletTransactionDto>> getWalletTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWalletTransactions(userId));
    }
}
