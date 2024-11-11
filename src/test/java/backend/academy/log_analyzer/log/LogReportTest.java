package backend.academy.log_analyzer.log;

import backend.academy.log_analyzer.enums.HttpCodes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты класса LogReport")
public class LogReportTest {

    @DisplayName("Тест корректности записи статистики")
    @Test
    void testAddAndUpdateStats() {
        List<Log> logs = createLogs();
        String uri = "file1";

        LogReport report = new LogReport();

        for (Log log : logs) {
            report.addAllStats(log, uri);
        }

        assertThat(report.getUniqueUsersCount()).isEqualTo(10);

        assertThat(report.getTotalCountRequests()).isEqualTo(10);

        List<Map.Entry<String, Long>> popularResources = report.getPopularResources();

        assertThat(popularResources.size()).isEqualTo(3);
        assertThat(popularResources.getFirst().getKey()).isEqualTo("/api/user/register");
        assertThat(popularResources.getFirst().getValue()).isEqualTo(3L);
        assertThat(popularResources.get(1).getKey()).isEqualTo("/downloads/product_2");
        assertThat(popularResources.get(1).getValue()).isEqualTo(2L);
        assertThat(popularResources.getLast().getKey()).isEqualTo("/api/resource/edit");
        assertThat(popularResources.getLast().getValue()).isEqualTo(1L);

        List<Map.Entry<HttpCodes, Long>> popularCodes = report.getPopularCodeResponses();

        assertThat(popularCodes.size()).isEqualTo(3);
        assertThat(popularCodes.getFirst().getKey()).isEqualTo(HttpCodes.OK);
        assertThat(popularCodes.getFirst().getValue()).isEqualTo(3L);
        assertThat(popularCodes.get(1).getKey()).isEqualTo(HttpCodes.NOT_MODIFIED);
        assertThat(popularCodes.get(1).getValue()).isEqualTo(2L);
        assertThat(popularCodes.getLast().getKey()).isEqualTo(HttpCodes.CREATED);
        assertThat(popularCodes.getLast().getValue()).isEqualTo(1L);

        assertThat(report.getAverageBytesSize()).isEqualTo(742L);

        assertThat(report.get95thPercentile()).isEqualTo(5678L);

        Map<Integer, Double> percentOfCodeResponses = report.getPercentOfCodeResponsesByType();

        assertThat(percentOfCodeResponses.get(200)).isEqualTo(40L);
        assertThat(percentOfCodeResponses.get(400)).isEqualTo(20L);
        assertThat(percentOfCodeResponses.get(500)).isEqualTo(20L);

        assertThat(report.resources()).containsExactly("file1");
    }

    private List<Log> createLogs() {
        return List.of(
                new Log("93.180.71.3", "-", LocalDateTime.of(2015, 5, 17, 8, 5, 32),
                        new Log.Request("GET", "/downloads/product_1", "HTTP/1.1"),
                        304, 0, "-", "Debian APT-HTTP/1.3"),

                new Log("66.249.73.185", "user1", LocalDateTime.of(2015, 6, 18, 11, 22, 45),
                        new Log.Request("POST", "/api/user/register", "HTTP/1.1"),
                        201, 1234, "-", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"),

                new Log("192.168.0.1", "user2", LocalDateTime.of(2016, 7, 22, 10, 5, 11),
                        new Log.Request("PUT", "/downloads/product_2", "HTTP/2"),
                        504, 5678, "/home", "Mozilla/5.0 (Linux x86_64)"),

                new Log("10.0.0.1", "-", LocalDateTime.of(2017, 8, 1, 13, 15, 55),
                        new Log.Request("DELETE", "/api/user/register", "HTTP/1.1"),
                        404, 0, "-", "curl/7.64.1"),

                new Log("203.0.113.10", "user3", LocalDateTime.of(2018, 9, 4, 9, 33, 2),
                        new Log.Request("PATCH", "/api/resource/edit", "HTTP/1.1"),
                        403, 512, "-", "PostmanRuntime/7.26.1"),

                new Log("198.51.100.42", "-", LocalDateTime.of(2019, 10, 6, 15, 17, 45),
                        new Log.Request("GET", "/downloads/product_2", "HTTP/1.1"),
                        304, 0, "-", "Debian APT-HTTP/1.3"),

                new Log("172.16.0.1", "user4", LocalDateTime.of(2020, 11, 9, 8, 20, 37),
                        new Log.Request("OPTIONS", "/api/check", "HTTP/2"),
                        200, 0, "-", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"),

                new Log("8.8.8.8", "-", LocalDateTime.of(2021, 12, 12, 19, 45, 8),
                        new Log.Request("HEAD", "/api/user/register", "HTTP/1.1"),
                        200, 0, "-", "curl/7.68.0"),

                new Log("1.1.1.1", "user5", LocalDateTime.of(2022, 1, 20, 22, 30, 59),
                        new Log.Request("GET", "/status", "HTTP/1.1"),
                        500, 0, "-", "PostmanRuntime/7.28.0"),

                new Log("127.0.0.1", "-", LocalDateTime.of(2023, 2, 28, 14, 0, 0),
                        new Log.Request("CONNECT", "/proxy", "HTTP/1.1"),
                        200, 0, "-", "Go-http-client/1.1")
        );
    }
}
