package com.stocktrade.config;

import com.stocktrade.entity.*;
import com.stocktrade.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing StockTrade database seed data...");

        // 1. Seed Users if not present
        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            User newAdmin = User.builder()
                    .name("System Administrator")
                    .email("admin@stocktrade.com")
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .walletBalance(new BigDecimal("1000000.00"))
                    .status(UserStatus.ACTIVE)
                    .createdAt(LocalDateTime.now().minusDays(30))
                    .build();
            return userRepository.save(newAdmin);
        });

        User trader = userRepository.findByUsername("trader").orElseGet(() -> {
            User newUser = User.builder()
                    .name("Satya Trader")
                    .email("user@stocktrade.com")
                    .username("trader")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.ROLE_USER)
                    .walletBalance(new BigDecimal("100000.00"))
                    .status(UserStatus.ACTIVE)
                    .createdAt(LocalDateTime.now().minusDays(15))
                    .build();
            return userRepository.save(newUser);
        });

        // 2. Seed Stocks if not present
        if (stockRepository.count() == 0) {
            List<Stock> sampleStocks = Arrays.asList(
                    Stock.builder()
                            .companyName("Reliance Industries Ltd")
                            .symbol("RELIANCE")
                            .sector("Energy & Conglomerate")
                            .currentPrice(new BigDecimal("2945.50"))
                            .openingPrice(new BigDecimal("2920.00"))
                            .previousClose(new BigDecimal("2915.00"))
                            .high(new BigDecimal("2960.00"))
                            .low(new BigDecimal("2910.00"))
                            .fiftyTwoWeekHigh(new BigDecimal("3024.90"))
                            .fiftyTwoWeekLow(new BigDecimal("2220.30"))
                            .updatedAt(LocalDateTime.now())
                            .build(),

                    Stock.builder()
                            .companyName("Tata Consultancy Services")
                            .symbol("TCS")
                            .sector("Information Technology")
                            .currentPrice(new BigDecimal("3890.00"))
                            .openingPrice(new BigDecimal("3850.00"))
                            .previousClose(new BigDecimal("3840.00"))
                            .high(new BigDecimal("3915.00"))
                            .low(new BigDecimal("3830.00"))
                            .fiftyTwoWeekHigh(new BigDecimal("4592.25"))
                            .fiftyTwoWeekLow(new BigDecimal("3313.00"))
                            .updatedAt(LocalDateTime.now())
                            .build(),

                    Stock.builder()
                            .companyName("Infosys Limited")
                            .symbol("INFY")
                            .sector("Information Technology")
                            .currentPrice(new BigDecimal("1540.25"))
                            .openingPrice(new BigDecimal("1525.00"))
                            .previousClose(new BigDecimal("1532.10"))
                            .high(new BigDecimal("1555.00"))
                            .low(new BigDecimal("1520.00"))
                            .fiftyTwoWeekHigh(new BigDecimal("1974.75"))
                            .fiftyTwoWeekLow(new BigDecimal("1358.35"))
                            .updatedAt(LocalDateTime.now())
                            .build(),

                    Stock.builder()
                            .companyName("HDFC Bank Limited")
                            .symbol("HDFCBANK")
                            .sector("Banking & Financials")
                            .currentPrice(new BigDecimal("1680.75"))
                            .openingPrice(new BigDecimal("1670.00"))
                            .previousClose(new BigDecimal("1665.50"))
                            .high(new BigDecimal("1692.00"))
                            .low(new BigDecimal("1660.00"))
                            .fiftyTwoWeekHigh(new BigDecimal("1794.00"))
                            .fiftyTwoWeekLow(new BigDecimal("1363.55"))
                            .updatedAt(LocalDateTime.now())
                            .build(),

                    Stock.builder()
                            .companyName("ICICI Bank Limited")
                            .symbol("ICICIBANK")
                            .sector("Banking & Financials")
                            .currentPrice(new BigDecimal("1120.40"))
                            .openingPrice(new BigDecimal("1105.00"))
                            .previousClose(new BigDecimal("1110.00"))
                            .high(new BigDecimal("1132.50"))
                            .low(new BigDecimal("1100.00"))
                            .fiftyTwoWeekHigh(new BigDecimal("1332.90"))
                            .fiftyTwoWeekLow(new BigDecimal("914.00"))
                            .updatedAt(LocalDateTime.now())
                            .build(),

                    Stock.builder()
                            .companyName("State Bank of India")
                            .symbol("SBIN")
                            .sector("Banking & Financials")
                            .currentPrice(new BigDecimal("815.60"))
                            .openingPrice(new BigDecimal("810.00"))
                            .previousClose(new BigDecimal("808.20"))
                            .high(new BigDecimal("824.00"))
                            .low(new BigDecimal("805.00"))
                            .fiftyTwoWeekHigh(new BigDecimal("912.10"))
                            .fiftyTwoWeekLow(new BigDecimal("555.30"))
                            .updatedAt(LocalDateTime.now())
                            .build(),

                    Stock.builder()
                            .companyName("Wipro Limited")
                            .symbol("WIPRO")
                            .sector("Information Technology")
                            .currentPrice(new BigDecimal("485.30"))
                            .openingPrice(new BigDecimal("482.00"))
                            .previousClose(new BigDecimal("488.50"))
                            .high(new BigDecimal("490.00"))
                            .low(new BigDecimal("480.00"))
                            .fiftyTwoWeekHigh(new BigDecimal("580.00"))
                            .fiftyTwoWeekLow(new BigDecimal("375.00"))
                            .updatedAt(LocalDateTime.now())
                            .build(),

                    Stock.builder()
                            .companyName("ITC Limited")
                            .symbol("ITC")
                            .sector("Consumer Goods (FMCG)")
                            .currentPrice(new BigDecimal("435.10"))
                            .openingPrice(new BigDecimal("432.00"))
                            .previousClose(new BigDecimal("430.00"))
                            .high(new BigDecimal("438.00"))
                            .low(new BigDecimal("431.00"))
                            .fiftyTwoWeekHigh(new BigDecimal("528.55"))
                            .fiftyTwoWeekLow(new BigDecimal("399.30"))
                            .updatedAt(LocalDateTime.now())
                            .build(),

                    Stock.builder()
                            .companyName("Bharti Airtel Limited")
                            .symbol("BHARTIARTL")
                            .sector("Telecommunication")
                            .currentPrice(new BigDecimal("1320.00"))
                            .openingPrice(new BigDecimal("1305.00"))
                            .previousClose(new BigDecimal("1300.00"))
                            .high(new BigDecimal("1335.00"))
                            .low(new BigDecimal("1298.00"))
                            .fiftyTwoWeekHigh(new BigDecimal("1779.00"))
                            .fiftyTwoWeekLow(new BigDecimal("915.00"))
                            .updatedAt(LocalDateTime.now())
                            .build(),

                    Stock.builder()
                            .companyName("Tata Motors Limited")
                            .symbol("TATAMOTORS")
                            .sector("Automobile")
                            .currentPrice(new BigDecimal("975.50"))
                            .openingPrice(new BigDecimal("960.00"))
                            .previousClose(new BigDecimal("965.00"))
                            .high(new BigDecimal("984.00"))
                            .low(new BigDecimal("955.00"))
                            .fiftyTwoWeekHigh(new BigDecimal("1179.05"))
                            .fiftyTwoWeekLow(new BigDecimal("618.00"))
                            .updatedAt(LocalDateTime.now())
                            .build()
            );

            stockRepository.saveAll(sampleStocks);
            log.info("Initialized 10 sample blue-chip stocks.");
        }

        // 3. Seed Initial Holdings and Transactions for demo trader if needed
        if (portfolioRepository.findByUserId(trader.getId()).isEmpty()) {
            Stock rel = stockRepository.findBySymbol("RELIANCE").orElse(null);
            Stock tcs = stockRepository.findBySymbol("TCS").orElse(null);
            Stock infy = stockRepository.findBySymbol("INFY").orElse(null);

            if (rel != null) {
                portfolioRepository.save(Portfolio.builder()
                        .user(trader)
                        .stock(rel)
                        .quantity(10)
                        .averageBuyPrice(new BigDecimal("2890.00"))
                        .build());

                transactionRepository.save(Transaction.builder()
                        .user(trader)
                        .stock(rel)
                        .transactionType(TransactionType.BUY)
                        .quantity(10)
                        .price(new BigDecimal("2890.00"))
                        .totalAmount(new BigDecimal("28900.00"))
                        .transactionDate(LocalDateTime.now().minusDays(5))
                        .status("COMPLETED")
                        .build());
            }

            if (tcs != null) {
                portfolioRepository.save(Portfolio.builder()
                        .user(trader)
                        .stock(tcs)
                        .quantity(5)
                        .averageBuyPrice(new BigDecimal("3820.00"))
                        .build());

                transactionRepository.save(Transaction.builder()
                        .user(trader)
                        .stock(tcs)
                        .transactionType(TransactionType.BUY)
                        .quantity(5)
                        .price(new BigDecimal("3820.00"))
                        .totalAmount(new BigDecimal("19100.00"))
                        .transactionDate(LocalDateTime.now().minusDays(3))
                        .status("COMPLETED")
                        .build());
            }

            if (infy != null) {
                portfolioRepository.save(Portfolio.builder()
                        .user(trader)
                        .stock(infy)
                        .quantity(15)
                        .averageBuyPrice(new BigDecimal("1510.00"))
                        .build());

                transactionRepository.save(Transaction.builder()
                        .user(trader)
                        .stock(infy)
                        .transactionType(TransactionType.BUY)
                        .quantity(15)
                        .price(new BigDecimal("1510.00"))
                        .totalAmount(new BigDecimal("22650.00"))
                        .transactionDate(LocalDateTime.now().minusDays(1))
                        .status("COMPLETED")
                        .build());
            }

            // Wallet seed deposits
            walletTransactionRepository.save(WalletTransaction.builder()
                    .user(trader)
                    .type(WalletTransactionType.DEPOSIT)
                    .amount(new BigDecimal("150000.00"))
                    .transactionDate(LocalDateTime.now().minusDays(10))
                    .status("COMPLETED")
                    .build());

            log.info("Initialized sample portfolio and transaction history for demo trader.");
        }

        log.info("StockTrade data initialization completed successfully.");
    }
}
