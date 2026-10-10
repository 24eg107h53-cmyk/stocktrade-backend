package com.stocktrade.service.impl;

import com.stocktrade.dto.StockDto;
import com.stocktrade.entity.Stock;
import com.stocktrade.exception.StockNotFoundException;
import com.stocktrade.repository.StockRepository;
import com.stocktrade.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StockDto> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::mapToStockDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockDto getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException("Stock not found with ID: " + id));
        return mapToStockDto(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public StockDto getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new StockNotFoundException("Stock not found with symbol: " + symbol));
        return mapToStockDto(stock);
    }

    @Override
    @Transactional
    public StockDto createStock(StockDto stockDto) {
        if (stockRepository.existsBySymbol(stockDto.getSymbol().toUpperCase())) {
            throw new IllegalArgumentException("Stock with symbol " + stockDto.getSymbol() + " already exists!");
        }

        BigDecimal price = stockDto.getCurrentPrice();
        BigDecimal open = stockDto.getOpeningPrice() != null ? stockDto.getOpeningPrice() : price;
        BigDecimal prev = stockDto.getPreviousClose() != null ? stockDto.getPreviousClose() : price;
        BigDecimal high = stockDto.getHigh() != null ? stockDto.getHigh() : price.max(open);
        BigDecimal low = stockDto.getLow() != null ? stockDto.getLow() : price.min(open);
        BigDecimal ftwHigh = stockDto.getFiftyTwoWeekHigh() != null ? stockDto.getFiftyTwoWeekHigh() : high;
        BigDecimal ftwLow = stockDto.getFiftyTwoWeekLow() != null ? stockDto.getFiftyTwoWeekLow() : low;

        Stock stock = Stock.builder()
                .companyName(stockDto.getCompanyName())
                .symbol(stockDto.getSymbol().toUpperCase())
                .sector(stockDto.getSector())
                .currentPrice(price)
                .openingPrice(open)
                .previousClose(prev)
                .high(high)
                .low(low)
                .fiftyTwoWeekHigh(ftwHigh)
                .fiftyTwoWeekLow(ftwLow)
                .updatedAt(LocalDateTime.now())
                .build();

        Stock saved = stockRepository.save(stock);
        return mapToStockDto(saved);
    }

    @Override
    @Transactional
    public StockDto updateStock(Long id, StockDto stockDto) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException("Stock not found with ID: " + id));

        stock.setCompanyName(stockDto.getCompanyName());
        stock.setSector(stockDto.getSector());

        if (stockDto.getCurrentPrice() != null) {
            stock.setCurrentPrice(stockDto.getCurrentPrice());
            if (stock.getCurrentPrice().compareTo(stock.getHigh()) > 0) {
                stock.setHigh(stock.getCurrentPrice());
            }
            if (stock.getCurrentPrice().compareTo(stock.getLow()) < 0) {
                stock.setLow(stock.getCurrentPrice());
            }
            if (stock.getCurrentPrice().compareTo(stock.getFiftyTwoWeekHigh()) > 0) {
                stock.setFiftyTwoWeekHigh(stock.getCurrentPrice());
            }
            if (stock.getCurrentPrice().compareTo(stock.getFiftyTwoWeekLow()) < 0) {
                stock.setFiftyTwoWeekLow(stock.getCurrentPrice());
            }
        }

        if (stockDto.getOpeningPrice() != null) stock.setOpeningPrice(stockDto.getOpeningPrice());
        if (stockDto.getPreviousClose() != null) stock.setPreviousClose(stockDto.getPreviousClose());
        if (stockDto.getHigh() != null) stock.setHigh(stockDto.getHigh());
        if (stockDto.getLow() != null) stock.setLow(stockDto.getLow());
        if (stockDto.getFiftyTwoWeekHigh() != null) stock.setFiftyTwoWeekHigh(stockDto.getFiftyTwoWeekHigh());
        if (stockDto.getFiftyTwoWeekLow() != null) stock.setFiftyTwoWeekLow(stockDto.getFiftyTwoWeekLow());

        Stock updated = stockRepository.save(stock);
        return mapToStockDto(updated);
    }

    @Override
    @Transactional
    public void deleteStock(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new StockNotFoundException("Stock not found with ID: " + id);
        }
        stockRepository.deleteById(id);
    }

    @Override
    @Transactional
    public StockDto updateStockPrice(Long id, BigDecimal newPrice) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException("Stock not found with ID: " + id));

        stock.setCurrentPrice(newPrice);
        if (newPrice.compareTo(stock.getHigh()) > 0) {
            stock.setHigh(newPrice);
        }
        if (newPrice.compareTo(stock.getLow()) < 0) {
            stock.setLow(newPrice);
        }
        if (newPrice.compareTo(stock.getFiftyTwoWeekHigh()) > 0) {
            stock.setFiftyTwoWeekHigh(newPrice);
        }
        if (newPrice.compareTo(stock.getFiftyTwoWeekLow()) < 0) {
            stock.setFiftyTwoWeekLow(newPrice);
        }

        Stock saved = stockRepository.save(stock);
        return mapToStockDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDto> searchStocks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllStocks();
        }
        return stockRepository.findByCompanyNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(query, query).stream()
                .map(this::mapToStockDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StockDto> simulateMarketTick() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            // Fluctuate price by between -1.5% and +1.5%
            double deltaPercent = (ThreadLocalRandom.current().nextDouble() * 3.0 - 1.5) / 100.0;
            BigDecimal current = stock.getCurrentPrice();
            BigDecimal change = current.multiply(BigDecimal.valueOf(deltaPercent));
            BigDecimal newPrice = current.add(change).setScale(2, RoundingMode.HALF_UP);

            if (newPrice.compareTo(new BigDecimal("1.00")) < 0) {
                newPrice = new BigDecimal("1.00");
            }

            stock.setCurrentPrice(newPrice);
            if (newPrice.compareTo(stock.getHigh()) > 0) {
                stock.setHigh(newPrice);
            }
            if (newPrice.compareTo(stock.getLow()) < 0) {
                stock.setLow(newPrice);
            }
        }
        List<Stock> saved = stockRepository.saveAll(stocks);
        return saved.stream().map(this::mapToStockDto).collect(Collectors.toList());
    }

    public StockDto mapToStockDto(Stock stock) {
        BigDecimal priceChange = BigDecimal.ZERO;
        BigDecimal percentageChange = BigDecimal.ZERO;

        if (stock.getPreviousClose() != null && stock.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
            priceChange = stock.getCurrentPrice().subtract(stock.getPreviousClose()).setScale(2, RoundingMode.HALF_UP);
            percentageChange = priceChange.divide(stock.getPreviousClose(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        return StockDto.builder()
                .id(stock.getId())
                .companyName(stock.getCompanyName())
                .symbol(stock.getSymbol())
                .sector(stock.getSector())
                .currentPrice(stock.getCurrentPrice())
                .openingPrice(stock.getOpeningPrice())
                .previousClose(stock.getPreviousClose())
                .high(stock.getHigh())
                .low(stock.getLow())
                .fiftyTwoWeekHigh(stock.getFiftyTwoWeekHigh())
                .fiftyTwoWeekLow(stock.getFiftyTwoWeekLow())
                .priceChange(priceChange)
                .percentageChange(percentageChange)
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}
