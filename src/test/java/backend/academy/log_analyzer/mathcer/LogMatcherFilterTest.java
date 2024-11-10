package backend.academy.log_analyzer.mathcer;

import backend.academy.log_analyzer.enums.OutputFormats;
import backend.academy.log_analyzer.log.Log;
import backend.academy.log_analyzer.matcher.LogMatcherFilter;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты класса LogMatcherFilter")
public class LogMatcherFilterTest {

    LogMatcherFilter logMatcherFilter = new LogMatcherFilter();

    @DisplayName("Тесты корректной фильтрации по ip")
    @ParameterizedTest(name = "поле: {0}, значение: {1}, значение в логе: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestFilterByIp")
    void testFilterByIp(String field, String value, String logValue, boolean expectedMath) {
        Log log = new Log(
                logValue,
                null,
                null,
                null,
                0,
                0,
                null,
                null

        );

        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                null,
                null,
                OutputFormats.TEXT,
                field,
                value
        );

        assertThat(logMatcherFilter.isLogMatchByFilter(log, argsParameters)).isEqualTo(expectedMath);
    }

    static Stream<Arguments> provideDataToTestFilterByIp() {
        return Stream.of(
                Arguments.of("ip", "100.12.09", "100.12.09", true),
                Arguments.of("ip", "100*", "100.98.223", true),
                Arguments.of("ip", "*.90.*", "200.90.123", true),
                Arguments.of("ip", "100.12.09", "100.12.08", false),
                Arguments.of("ip", "100*", "105.98.223", false),
                Arguments.of("ip", "*.90.*", "200.40.123", false)
        );
    }

    @DisplayName("Тесты корректной фильтрации по user")
    @ParameterizedTest(name = "поле: {0}, значение: {1}, значение в логе: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestFilterByUser")
    void testFilterByUser(String field, String value, String logValue, boolean expectedMath) {
        Log log = new Log(
                null,
                logValue,
                null,
                null,
                0,
                0,
                null,
                null

        );

        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                null,
                null,
                OutputFormats.TEXT,
                field,
                value
        );

        assertThat(logMatcherFilter.isLogMatchByFilter(log, argsParameters)).isEqualTo(expectedMath);
    }

    static Stream<Arguments> provideDataToTestFilterByUser() {
        return Stream.of(
                Arguments.of("user", "admin", "admin", true),
                Arguments.of("user", "user*", "user123", true),
                Arguments.of("user", "*user*", "testuser123", true),
                Arguments.of("user", "admin", "guest", false),
                Arguments.of("user", "user*", "guest123", false),
                Arguments.of("user", "*user*", "guest", false)
        );
    }

    @DisplayName("Тесты корректной фильтрации по time")
    @ParameterizedTest(name = "поле: {0}, значение: {1}, значение в логе: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestFilterByTime")
    void testFilterByTime(String field, String value, String logValue, boolean expectedMath) {
        Log log = new Log(
                null,
                null,
                LocalDateTime.parse(logValue),
                null,
                0,
                0,
                null,
                null

        );

        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                null,
                null,
                OutputFormats.TEXT,
                field,
                value
        );

        assertThat(logMatcherFilter.isLogMatchByFilter(log, argsParameters)).isEqualTo(expectedMath);
    }

    static Stream<Arguments> provideDataToTestFilterByTime() {
        return Stream.of(
                Arguments.of("time", "2023-05-10T10:15:30", "2023-05-10T10:15:30", true),
                Arguments.of("time", "2023-05*", "2023-05-15T12:00:00", true),
                Arguments.of("time", "*2023*", "2023-07-01T15:45:30", true),
                Arguments.of("time", "2023-05-10T10:15:30", "2023-05-10T10:15:31", false),
                Arguments.of("time", "2023-05*", "2024-05-15T12:00:00", false),
                Arguments.of("time", "*2023*", "2024-01-01T00:00:00", false)
        );
    }

    @DisplayName("Тесты корректной фильтрации по method")
    @ParameterizedTest(name = "поле: {0}, значение: {1}, значение в логе: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestFilterByMethod")
    void testFilterByMethod(String field, String value, String logValue, boolean expectedMath) {
        Log log = new Log(
                null,
                null,
                null,
                new Log.Request(
                        logValue,
                        "",
                        ""
                ),
                0,
                0,
                null,
                null

        );

        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                null,
                null,
                OutputFormats.TEXT,
                field,
                value
        );

        assertThat(logMatcherFilter.isLogMatchByFilter(log, argsParameters)).isEqualTo(expectedMath);
    }

    static Stream<Arguments> provideDataToTestFilterByMethod() {
        return Stream.of(

                Arguments.of("method", "GET", "GET", true),
                Arguments.of("method", "POST*", "POSTDATA", true),
                Arguments.of("method", "*DELETE*", "SOFTDELETE", true),
                Arguments.of("method", "GET", "POST", false),
                Arguments.of("method", "POST*", "GETDATA", false),
                Arguments.of("method", "*DELETE*", "REMOVETRANSFER", false)
        );
    }

    @DisplayName("Тесты корректной фильтрации по endpoint")
    @ParameterizedTest(name = "поле: {0}, значение: {1}, значение в логе: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestFilterByEndpoint")
    void testFilterByEndpoint(String field, String value, String logValue, boolean expectedMath) {
        Log log = new Log(
                null,
                null,
                null,
                new Log.Request(
                        "",
                        logValue,
                        ""
                ),
                0,
                0,
                null,
                null

        );

        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                null,
                null,
                OutputFormats.TEXT,
                field,
                value
        );

        assertThat(logMatcherFilter.isLogMatchByFilter(log, argsParameters)).isEqualTo(expectedMath);
    }

    static Stream<Arguments> provideDataToTestFilterByEndpoint() {
        return Stream.of(
                Arguments.of("endpoint", "/home", "/home", true),
                Arguments.of("endpoint", "/api*", "/api/v1/resource", true),
                Arguments.of("endpoint", "*/resource*", "/api/v1/resource", true),
                Arguments.of("endpoint", "/home", "/login", false),
                Arguments.of("endpoint", "/api*", "/service/v2", false),
                Arguments.of("endpoint", "*/resource*", "/user/details", false)
        );
    }

    @DisplayName("Тесты корректной фильтрации по version")
    @ParameterizedTest(name = "поле: {0}, значение: {1}, значение в логе: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestFilterByVersion")
    void testFilterByVersion(String field, String value, String logValue, boolean expectedMath) {
        Log log = new Log(
                null,
                null,
                null,
                new Log.Request(
                        "",
                        "",
                        logValue
                ),
                0,
                0,
                null,
                null

        );

        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                null,
                null,
                OutputFormats.TEXT,
                field,
                value
        );

        assertThat(logMatcherFilter.isLogMatchByFilter(log, argsParameters)).isEqualTo(expectedMath);
    }

    static Stream<Arguments> provideDataToTestFilterByVersion() {
        return Stream.of(
                Arguments.of("version", "HTTP/1.1", "HTTP/1.1", true),
                Arguments.of("version", "HTTP/1.*", "HTTP/1.0", true),
                Arguments.of("version", "*1.1*", "HTTP/1.1", true),
                Arguments.of("version", "HTTP/1.1", "HTTP/2.0", false),
                Arguments.of("version", "HTTP/1.*", "HTTP/2.0", false),
                Arguments.of("version", "*1.1*", "HTTP/2.1", false)
        );
    }

    @DisplayName("Тесты корректной фильтрации по status")
    @ParameterizedTest(name = "поле: {0}, значение: {1}, значение в логе: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestFilterByStatus")
    void testFilterByStatus(String field, String value, String logValue, boolean expectedMath) {
        Log log = new Log(
                null,
                null,
                null,
                new Log.Request(
                        "",
                        "",
                        ""
                ),
                Integer.parseInt(logValue),
                0,
                null,
                null

        );

        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                null,
                null,
                OutputFormats.TEXT,
                field,
                value
        );

        assertThat(logMatcherFilter.isLogMatchByFilter(log, argsParameters)).isEqualTo(expectedMath);
    }

    static Stream<Arguments> provideDataToTestFilterByStatus() {
        return Stream.of(
                Arguments.of("status", "200", "200", true),
                Arguments.of("status", "2*", "201", true),
                Arguments.of("status", "*0", "200", true),
                Arguments.of("status", "200", "404", false),
                Arguments.of("status", "2*", "500", false),
                Arguments.of("status", "*0", "404", false)

        );
    }

    @DisplayName("Тесты корректной фильтрации по bytes")
    @ParameterizedTest(name = "поле: {0}, значение: {1}, значение в логе: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestFilterByBytes")
    void testFilterByBytes(String field, String value, String logValue, boolean expectedMath) {
        Log log = new Log(
                null,
                null,
                null,
                new Log.Request(
                        "",
                        "",
                        ""
                ),
                0,
                Integer.parseInt(logValue),
                null,
                null

        );

        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                null,
                null,
                OutputFormats.TEXT,
                field,
                value
        );

        assertThat(logMatcherFilter.isLogMatchByFilter(log, argsParameters)).isEqualTo(expectedMath);
    }

    static Stream<Arguments> provideDataToTestFilterByBytes() {
        return Stream.of(

                Arguments.of("bytes", "1024", "1024", true),
                Arguments.of("bytes", "10*", "1048", true),
                Arguments.of("bytes", "*24", "1024", true),
                Arguments.of("bytes", "1024", "512", false),
                Arguments.of("bytes", "10*", "5120", false),
                Arguments.of("bytes", "*24", "512", false)

        );
    }

    @DisplayName("Тесты корректной фильтрации по referer")
    @ParameterizedTest(name = "поле: {0}, значение: {1}, значение в логе: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestFilterByReferer")
    void testFilterByReferer(String field, String value, String logValue, boolean expectedMath) {
        Log log = new Log(
                null,
                null,
                null,
                new Log.Request(
                        "",
                        "",
                        ""
                ),
                0,
                0,
                logValue,
                null

        );

        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                null,
                null,
                OutputFormats.TEXT,
                field,
                value
        );

        assertThat(logMatcherFilter.isLogMatchByFilter(log, argsParameters)).isEqualTo(expectedMath);
    }

    static Stream<Arguments> provideDataToTestFilterByReferer() {
        return Stream.of(
                Arguments.of("referer", "http://example.com", "http://example.com", true),
                Arguments.of("referer", "http://*", "http://example.org", true),
                Arguments.of("referer", "*example*", "http://example.com/page", true),
                Arguments.of("referer", "http://example.com", "http://other.com", false),
                Arguments.of("referer", "http://*", "https://example.com", false),
                Arguments.of("referer", "*example*", "http://site.com", false)

        );
    }

    @DisplayName("Тесты корректной фильтрации по agent")
    @ParameterizedTest(name = "поле: {0}, значение: {1}, значение в логе: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestFilterByAgent")
    void testFilterByAgent(String field, String value, String logValue, boolean expectedMath) {
        Log log = new Log(
                null,
                null,
                null,
                new Log.Request(
                        "",
                        "",
                        ""
                ),
                0,
                0,
                null,
                logValue

        );

        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                null,
                null,
                OutputFormats.TEXT,
                field,
                value
        );

        assertThat(logMatcherFilter.isLogMatchByFilter(log, argsParameters)).isEqualTo(expectedMath);
    }

    static Stream<Arguments> provideDataToTestFilterByAgent() {
        return Stream.of(
                Arguments.of("agent", "Mozilla/5.0", "Mozilla/5.0", true),
                Arguments.of("agent", "Mozilla*", "Mozilla/5.0 (Windows)", true),
                Arguments.of("agent", "*Windows*", "Mozilla/5.0 (Windows NT 10.0)", true),
                Arguments.of("agent", "Mozilla/5.0", "Safari/537.36", false),
                Arguments.of("agent", "Mozilla*", "Safari/537.36", false),
                Arguments.of("agent", "*Windows*", "Mozilla/5.0 (Linux)", false)
        );
    }
}
