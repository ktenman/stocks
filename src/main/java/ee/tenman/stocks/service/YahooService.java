package ee.tenman.stocks.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.decampo.xirr.Transaction;
import org.decampo.xirr.Xirr;
import org.springframework.stereotype.Service;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;

@Service
@Slf4j
public class YahooService {

    @SneakyThrows
    public void stock(String ticker) {
        LocalDateTime now = LocalDateTime.now();
        Calendar to = calendar(now);
        Calendar from = calendar(now.minusYears(30));


        Stock stock = YahooFinance.get(ticker, from, to, Interval.MONTHLY);
        List<HistoricalQuote> history = stock.getHistory();
        int totalBoughtStocksCount = 0;
        BigDecimal originalBigDecimal = new BigDecimal("2000.00");
        BigDecimal amountToSpend = originalBigDecimal;
        List<Transaction> transactions = new ArrayList<>();

        for (HistoricalQuote historicalQuote : history) {
            int stocksCount = amountToSpend.divide(historicalQuote.getClose(), RoundingMode.DOWN).intValue();
            totalBoughtStocksCount += stocksCount;
            BigDecimal spent = BigDecimal.valueOf(stocksCount).multiply(historicalQuote.getClose());

            amountToSpend = amountToSpend.subtract(spent).add(originalBigDecimal);
            LocalDate localDate = LocalDateTime.ofInstant(historicalQuote.getDate().toInstant(), UTC).toLocalDate();
            transactions.add(new Transaction(spent.doubleValue(), localDate));
        }

        BigDecimal lastPrice = history.get(history.size() - 1).getClose();
        double totalValue = BigDecimal.valueOf(totalBoughtStocksCount).negate().multiply(lastPrice).doubleValue();
        transactions.add(new Transaction(totalValue, LocalDate.now()));

        Xirr xirr = new Xirr(transactions);
        log.info("{} : {}%", ticker, xirr.xirr() * 100);
    }

    Calendar calendar(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(UTC).toInstant();
        Date date = Date.from(instant);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC));
        calendar.setTime(date);
        return calendar;
    }
}