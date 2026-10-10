package com.stocktrade.dto;

import com.stocktrade.entity.TransactionType;
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
public class TradeResponse {
    private Long transactionId;
    private Long userId;
    private Long stockId;
    private String stockSymbol;
    private String companyName;
    private TransactionType transactionType;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private LocalDateTime transactionDate;
    private String status;
    private String message;
    private BigDecimal remainingWalletBalance;
    private Integer remainingStockQuantity;
}
