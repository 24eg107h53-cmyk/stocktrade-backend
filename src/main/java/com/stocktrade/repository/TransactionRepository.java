package com.stocktrade.repository;

import com.stocktrade.entity.Transaction;
import com.stocktrade.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    List<Transaction> findAllByOrderByTransactionDateDesc();
    Long countByTransactionType(TransactionType transactionType);

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE t.status = 'COMPLETED'")
    BigDecimal calculateTotalTradingVolume();
}
