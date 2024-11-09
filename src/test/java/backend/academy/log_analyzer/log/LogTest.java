package backend.academy.log_analyzer.log;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты record Log")
public class LogTest {

    @DisplayName("Тест парсинга строки лога в объект Log")
    @ParameterizedTest(name = "строка лога = {0}, ожидаемый объект: {1}")
    @MethodSource("provideDataTestParseLog")
    void testParseLog(String logLine, Log expectedLog) {
        Log actualLog = Log.parse(logLine);

        assertThat(actualLog).isEqualTo(expectedLog);
    }

    static Stream<Arguments> provideDataTestParseLog() {
        return Stream.of(
            Arguments.of(
                "114.80.245.62 - - [18/May/2015:05:05:22 +0000] \"GET /downloads/product_2 HTTP/1.1\" "
                    + "200 26318005 \"-\" \"Mozilla/5.0 (Windows NT 5.1) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.104 Safari/537.36\"",
                new Log(
                    "114.80.245.62",
                    "-",
                    LocalDateTime.of(2015, 5, 18, 5, 5, 22),
                    new Log.Request(
                        "GET",
                        "/downloads/product_2",
                        "HTTP/1.1"
                    ),
                    200,
                    26318005,
                    "-",
                    "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/38.0.2125.104 Safari/537.36"
                )
            ),
            Arguments.of(
                "148.251.112.153 - - [17/May/2015:23:05:12 +0000] \"GET /downloads/product_2 HTTP/1.1\" "
                    + "404 333 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.17)\"",
                new Log(
                    "148.251.112.153",
                    "-",
                    LocalDateTime.of(2015, 5, 17, 23, 5, 12),
                    new Log.Request(
                        "GET",
                        "/downloads/product_2",
                        "HTTP/1.1"
                    ),
                    404,
                    333,
                    "-",
                    "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.17)"
                )
            )
        );
    }

    @DisplayName("Тест парсинга, если подан лог в неверном формате")
    @Test
    void testParseLogWithInvalidFormat() {
        String logLine = "18:22:54.831 INFO  [main           ] backend.academy.Main                -- Hello World!";

        Log log = Log.parse(logLine);

        assertThat(log).isNull();
    }
}
