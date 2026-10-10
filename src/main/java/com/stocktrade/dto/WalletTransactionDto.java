package com.stocktrade.dto;

import com.stocktrade.entity.WalletTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionDto {
    private Long id;
    private Long userId;
    private String userName;
    private WalletTransactionType type;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String status;
}
