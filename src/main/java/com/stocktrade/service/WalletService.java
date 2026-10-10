package com.stocktrade.service;

import com.stocktrade.dto.WalletRequest;
import com.stocktrade.dto.WalletResponse;
import com.stocktrade.dto.WalletTransactionDto;

import java.util.List;

public interface WalletService {
    WalletResponse getWalletInfo(Long userId);
    WalletResponse depositMoney(WalletRequest walletRequest, String authenticatedUsername);
    WalletResponse withdrawMoney(WalletRequest walletRequest, String authenticatedUsername);
    List<WalletTransactionDto> getWalletTransactions(Long userId);
}
