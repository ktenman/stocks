package ee.tenman.stocks;

import com.codeborne.selenide.Configuration;
import ee.tenman.stocks.service.YahooService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

@SpringBootTest
class StocksApplicationTests {

    @Resource
    YahooService yahooService;

    @ParameterizedTest
    @ValueSource(strings = {"QDVE.DE", "EXXT.DE"})
    void stock(String ticker) {
        yahooService.stock(ticker);
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXXT.DE", "TWLO", "QDVE.DE", "IITU.L", "EQQQ.DE"})
    void stock2(String ticker) {
        double percentage = yahooService.stock(ticker);
        System.out.println("Percentage: " + percentage);
        for (int i = 1; i < 31; i++) {
            yahooService.stock2(ticker, i, percentage);
        }
    }

    @Test
    void stock4() {
        double percentage = yahooService.stock("QDVE.DE");
        double percentage2 = yahooService.stock("IITU.L");
        percentage = (percentage + percentage2) / 2.00000;
        System.out.println("Percentage: " + percentage);
        for (int i = 1; i < 31; i++) {
            yahooService.stock2("QDVE.DE", i, percentage);
        }
    }

    @Test
    void name() {
        BigDecimal starting = new BigDecimal("2952078.27");
        for (int i = 1; i < 16; i++) {
            yahooService.stock3(starting, i, new BigDecimal("5000.00"));
        }
    }

    @Test
    void name3() {
        Configuration.browser = "firefox";
        Configuration.headless = false;
        open("http://google.com");
        $(By.name("user.name")).setValue("johny");
        $("#submit").click();
        $(".loading_progress").should(disappear); // Waits until element disappears
        $("#username").shouldHave(text("Hello, Johny!")); // Waits until element gets text

    }
}
