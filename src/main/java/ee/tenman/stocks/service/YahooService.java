package ee.tenman.stocks.service;

import ee.tenman.stocks.service.xirr.Transaction;
import ee.tenman.stocks.service.xirr.Xirr;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;

@Service
@Slf4j
public class YahooService {

    DecimalFormat df = new DecimalFormat("#,###.00");

    @SneakyThrows
    public double stock(String ticker) {
        LocalDateTime now = LocalDateTime.now();
        Calendar to = calendar(now);
        Calendar from = calendar(now.minusYears(30));
//        Calendar from = calendar(now.minusYears(100));

        Stock stock = YahooFinance.get(ticker, from, to, Interval.MONTHLY);
        List<HistoricalQuote> history = stock.getHistory();
        int totalBoughtStocksCount = 0;
        BigDecimal originalBigDecimal = new BigDecimal("3000.00");
        BigDecimal amountToSpend = originalBigDecimal;
        List<Transaction> transactions = new ArrayList<>();

        List<BigDecimal> totalSpent = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        BigDecimal lastPrice = BigDecimal.ONE;
        int count = 0;
        for (HistoricalQuote historicalQuote : history) {
            if (historicalQuote.getClose() != null) {
                lastPrice = historicalQuote.getClose();
            }
            BigDecimal price = Optional.ofNullable(historicalQuote.getClose()).orElse(lastPrice);
            int stocksCount = amountToSpend.divide(price, RoundingMode.DOWN).intValue();
            totalBoughtStocksCount += stocksCount;
            BigDecimal spent = BigDecimal.valueOf(stocksCount).multiply(price);
            totalSpent.add(spent);

            amountToSpend = amountToSpend.subtract(spent).add(originalBigDecimal);
            LocalDate localDate = LocalDateTime.ofInstant(historicalQuote.getDate().toInstant(), UTC).toLocalDate();
            transactions.add(new Transaction(spent.doubleValue(), localDate));
            dates.add(localDate);
        }

        BigDecimal lastPrice2 = history.get(history.size() - 1).getClose();
        double totalValue = BigDecimal.valueOf(totalBoughtStocksCount).negate().multiply(lastPrice2).doubleValue();
        LocalDate now1 = LocalDate.now();
        transactions.add(new Transaction(totalValue, now1));

        Xirr xirr = new Xirr(transactions);
        log.info("{} : {}%", ticker, xirr.xirr() * 100);
        BigDecimal reduce = totalSpent.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate min = dates.stream().min(LocalDate::compareTo).orElseThrow();
        log.info("Total value: {}, Amount spent: {}", BigDecimal.valueOf(totalValue).negate(), reduce);
        log.info("Years: {}, Months: {}, Dayes: {}",
                YEARS.between(min, now1),
                MONTHS.between(min, now1),
                DAYS.between(min, now1)
        );
        return xirr.xirr() + 1;
    }

    @SneakyThrows
    public void stock2(String ticker, int year, double percentage) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now;
        LocalDateTime to = now.plusYears(year);

        int totalBoughtStocksCount = 0;
        BigDecimal originalBigDecimal = new BigDecimal("4000.00");
        BigDecimal amountToSpend = originalBigDecimal;
        List<Transaction> transactions = new ArrayList<>();
        List<Something> history = new ArrayList<>();
        BigDecimal price = new BigDecimal("10.000");
        while (from.isBefore(to)) {
            from = from.plusMonths(1);
            double pow = Math.pow(percentage, (1 / 12.00000000000000000000));
            price = price.multiply(BigDecimal.valueOf(pow));
            history.add(Something.builder().price(price).date(from).build());
        }
        List<BigDecimal> result = new ArrayList<>();
        int count = 0;
        for (Something something : history) {
            count++;
//            if (count <= 4) {
//                originalBigDecimal = new BigDecimal("1000.00");
//            } else if (count <= 8) {
//                originalBigDecimal = new BigDecimal("2000.00");
//            } else if (count <= 12) {
//                originalBigDecimal = new BigDecimal("3000.00");
//            } else if (count <= 13) {
//                originalBigDecimal = new BigDecimal("4000.00");
//            }

//            if (count == 1) {
//                originalBigDecimal = new BigDecimal("1000.00");
//            } else if (count <= 12) {
//                originalBigDecimal = originalBigDecimal.multiply(new BigDecimal("1.11909002782829"));
//            } else if (count <= 13) {
//                originalBigDecimal = new BigDecimal("4000.00");
//            }

            if (count <= 6) {
                originalBigDecimal = BigDecimal.ZERO;
            } else if (count <= 12) {
                originalBigDecimal = new BigDecimal("4000.00");
            }

            int stocksCount = amountToSpend.divide(something.getPrice(), RoundingMode.DOWN).intValue();
            totalBoughtStocksCount += stocksCount;
            BigDecimal spent = BigDecimal.valueOf(stocksCount).multiply(something.getPrice());
            result.add(spent);

            amountToSpend = amountToSpend.subtract(spent).add(originalBigDecimal);
            LocalDate localDate = something.getDate().toLocalDate();
            transactions.add(new Transaction(spent.doubleValue(), localDate));
            originalBigDecimal = originalBigDecimal.multiply(BigDecimal.valueOf(Math.pow(1.05, (1 / 12.0))));
        }

        BigDecimal lastPrice = history.get(history.size() - 1).getPrice();
        double totalValue = BigDecimal.valueOf(totalBoughtStocksCount).negate().multiply(lastPrice).doubleValue();
        transactions.add(new Transaction(totalValue, to.toLocalDate()));

        Xirr xirr = new Xirr(transactions);
        BigDecimal invested = result.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal earned = BigDecimal.valueOf(totalValue).negate();
        log.info("Salary: {}, Earned: {}%, Year: {}, Invested: {}, Total value: {}, Percentage: {}%, Ticker {} : {}%",
                df.format(amountToSpend.multiply(BigDecimal.valueOf(12))),
                df.format(amountToSpend.multiply(BigDecimal.valueOf(12)).divide(earned, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100))),
                year,
                df.format(invested),
                df.format(earned),
                df.format(earned.divide(invested, RoundingMode.DOWN).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100))),
                ticker,
                df.format(xirr.xirr() * 100)
        );
    }

    @SneakyThrows
    public void stock3(BigDecimal starting, int year, BigDecimal substract) {
        BigDecimal percent = new BigDecimal("1.1801740336101577");
        for (int i = 1; i <= year; i++) {
            starting = starting.multiply(percent);
            starting = starting.subtract(substract.multiply(BigDecimal.valueOf(12)));
            substract = substract.multiply(new BigDecimal("1.05"));
        }
        log.info("Year: {}, Value: {}", year, df.format(starting));
    }

    Calendar calendar(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(UTC).toInstant();
        Date date = Date.from(instant);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC));
        calendar.setTime(date);
        return calendar;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Something {
        private LocalDateTime date;
        private BigDecimal price;
    }
}