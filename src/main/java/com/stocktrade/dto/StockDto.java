package com.stocktrade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class StockDto {
    private Long id;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotBlank(message = "Sector is required")
    private String sector;

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal currentPrice;

    @NotNull(message = "Opening price is required")
    @DecimalMin(value = "0.01", message = "Opening price must be greater than zero")
    private BigDecimal openingPrice;

    @NotNull(message = "Previous close is required")
    @DecimalMin(value = "0.01", message = "Previous close must be greater than zero")
    private BigDecimal previousClose;

    @NotNull(message = "High price is required")
    private BigDecimal high;

    @NotNull(message = "Low price is required")
    private BigDecimal low;

    @NotNull(message = "52-week high is required")
    private BigDecimal fiftyTwoWeekHigh;

    @NotNull(message = "52-week low is required")
    private BigDecimal fiftyTwoWeekLow;

    private BigDecimal priceChange;
    private BigDecimal percentageChange;

    private LocalDateTime updatedAt;
}
