package ee.tenman.stocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StocksApplication {

	public static void main(String[] args) {
		SpringApplication.run(StocksApplication.class, args);
	}

}
