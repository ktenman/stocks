package ee.tenman.stocks;

import ee.tenman.stocks.service.YahooService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class StocksApplicationTests {

	@Resource
	YahooService yahooService;

	@ParameterizedTest
	@ValueSource(strings = {"QDVE.DE", "EXXT.DE"})
	void contextLoads(String ticker) {
		yahooService.stock(ticker);
	}

}
