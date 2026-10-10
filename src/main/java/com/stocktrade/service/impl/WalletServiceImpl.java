package com.stocktrade.service.impl;

import com.stocktrade.dto.WalletRequest;
import com.stocktrade.dto.WalletResponse;
import com.stocktrade.dto.WalletTransactionDto;
import com.stocktrade.entity.User;
import com.stocktrade.entity.WalletTransaction;
import com.stocktrade.entity.WalletTransactionType;
import com.stocktrade.exception.InsufficientBalanceException;
import com.stocktrade.exception.InvalidTransactionException;
import com.stocktrade.exception.UserNotFoundException;
import com.stocktrade.repository.UserRepository;
import com.stocktrade.repository.WalletTransactionRepository;
import com.stocktrade.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        List<WalletTransaction> txs = walletTransactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId());

        BigDecimal totalDeposits = txs.stream()
                .filter(t -> t.getType() == WalletTransactionType.DEPOSIT && "COMPLETED".equals(t.getStatus()))
                .map(WalletTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawals = txs.stream()
                .filter(t -> t.getType() == WalletTransactionType.WITHDRAW && "COMPLETED".equals(t.getStatus()))
                .map(WalletTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return WalletResponse.builder()
                .userId(user.getId())
                .balance(user.getWalletBalance().setScale(2, RoundingMode.HALF_UP))
                .totalDeposits(totalDeposits.setScale(2, RoundingMode.HALF_UP))
                .totalWithdrawals(totalWithdrawals.setScale(2, RoundingMode.HALF_UP))
                .message("Wallet balance fetched successfully")
                .build();
    }

    @Override
    @Transactional
    public WalletResponse depositMoney(WalletRequest walletRequest, String authenticatedUsername) {
        User user = resolveUser(walletRequest.getUserId(), authenticatedUsername);

        if (walletRequest.getAmount() == null || walletRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Deposit amount must be greater than zero.");
        }

        user.setWalletBalance(user.getWalletBalance().add(walletRequest.getAmount()));
        userRepository.save(user);

        WalletTransaction wt = WalletTransaction.builder()
                .user(user)
                .type(WalletTransactionType.DEPOSIT)
                .amount(walletRequest.getAmount().setScale(2, RoundingMode.HALF_UP))
                .transactionDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();
        walletTransactionRepository.save(wt);

        return getWalletInfo(user.getId());
    }

    @Override
    @Transactional
    public WalletResponse withdrawMoney(WalletRequest walletRequest, String authenticatedUsername) {
        User user = resolveUser(walletRequest.getUserId(), authenticatedUsername);

        if (walletRequest.getAmount() == null || walletRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be greater than zero.");
        }

        if (user.getWalletBalance().compareTo(walletRequest.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient wallet balance for withdrawal! Current balance: ₹%s, Requested: ₹%s",
                            user.getWalletBalance(), walletRequest.getAmount())
            );
        }

        user.setWalletBalance(user.getWalletBalance().subtract(walletRequest.getAmount()));
        userRepository.save(user);

        WalletTransaction wt = WalletTransaction.builder()
                .user(user)
                .type(WalletTransactionType.WITHDRAW)
                .amount(walletRequest.getAmount().setScale(2, RoundingMode.HALF_UP))
                .transactionDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();
        walletTransactionRepository.save(wt);

        return getWalletInfo(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionDto> getWalletTransactions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return walletTransactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId()).stream()
                .map(wt -> WalletTransactionDto.builder()
                        .id(wt.getId())
                        .userId(user.getId())
                        .userName(user.getName())
                        .type(wt.getType())
                        .amount(wt.getAmount())
                        .transactionDate(wt.getTransactionDate())
                        .status(wt.getStatus())
                        .build())
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
}
