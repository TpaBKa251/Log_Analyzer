package backend.academy.log_analyzer.log;

import backend.academy.log_analyzer.maper.ReportMapper;
import backend.academy.log_analyzer.matcher.LogMatcherDate;
import backend.academy.log_analyzer.matcher.LogMatcherFilter;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты класса LogAnalyzer")
public class LogAnalyzerTest {

    Map<BufferedReader, URI> readers = new HashMap<>();

    @Mock
    LogMatcherDate logMatcherDate;

    @Mock
    LogMatcherFilter logMatcherFilter;

    @Mock
    ReportMapper reportMapper;

    @Mock
    LogReport logReport;

    @Mock
    ArgsParameters parameters;

    @InjectMocks
    LogAnalyzer logAnalyzer;

    @TempDir
    static Path tempDir;

    WireMockServer wireMockServer;

    static ByteArrayOutputStream outputStream;
    static PrintStream originalSystemOut = System.out;

    @BeforeAll
    static void setTempDir() throws IOException {
        Path nestedDir1 = tempDir.resolve("logsDir/logs/some-logs/2024");
        Path nestedDir2 = tempDir.resolve("logsDir/logs/some-logs/2024_08");

        Files.createDirectories(nestedDir1);
        Files.createDirectories(nestedDir2);

        Path dir2 = tempDir.resolve("logsDir/logs/some-logs");
        Path dir3 = tempDir.resolve("logsDir/logs/some-logs/2024");

        Files.createFile(dir2.resolve("05-2023.txt"));
        Files.createFile(dir3.resolve("05"));

        Path sourceFile2 = Paths.get("src/main/resources/logs/some-logs/05-2023.txt");
        Path sourceFile5 = Paths.get("src/main/resources/logs/some-logs/2024/05");

        Files.copy(sourceFile2, dir2.resolve("05-2023.txt"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile5, dir3.resolve("05"), StandardCopyOption.REPLACE_EXISTING);

        outputStream = new ByteArrayOutputStream();
        originalSystemOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void closeReaders() {
        readers.keySet().forEach(r -> {
            try {
                r.close();
            } catch (IOException ignored) {
            }
        });

        System.setOut(originalSystemOut);
    }

    @DisplayName("Тест чтения логов из файлов")
    @Test
    void testAnalyzeLogsFromFiles() throws IOException {
        // Здесь 14 логов
        readers.put(
            new BufferedReader(new FileReader(tempDir.resolve("logsDir/logs/some-logs/2024/05").toFile(),
                StandardCharsets.UTF_8)),
            tempDir.resolve("logsDir/logs/some-logs/2024/05").toUri()
        );
        // Здесь 8 логов
        readers.put(
            new BufferedReader(new FileReader(tempDir.resolve("logsDir/logs/some-logs/05-2023.txt").toFile(),
                StandardCharsets.UTF_8)),
            tempDir.resolve("logsDir/logs/some-logs/05-2023.txt").toUri()
        );
        // Суммарно логов 22

        logAnalyzer = new LogAnalyzer(logMatcherDate, logMatcherFilter, reportMapper, logReport, readers);

        lenient().when(logMatcherDate.isLogMatch(any(LocalDateTime.class), eq(parameters))).thenReturn(true);
        lenient().when(logMatcherFilter.isLogMatchByFilter(any(Log.class), eq(parameters))).thenReturn(true);
        lenient().when(logReport.getTotalCountRequests()).thenReturn(1L);
        lenient().when(reportMapper.mapLogToOutputFormat(logReport, parameters)).thenReturn("Test Report");

        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.parse(anyString())).thenReturn(new Log(
                "entry",
                "description",
                LocalDateTime.now(),
                null,
                0,
                0,
                "field",
                "value"
            ));

            String result = logAnalyzer.analyzeLogs(parameters);

            verify(logReport, times(22)).addAllStats(any(), anyString());
            assertThat(result).isEqualTo("Test Report");
        }
    }

    @DisplayName("Тест чтения логов из файла, если не подошел ни один лог")
    @Test
    void testAnalyzeLogsFromFilesIfNoMatchLogs() throws IOException {
        readers.put(
            new BufferedReader(new FileReader(tempDir.resolve("logsDir/logs/some-logs/2024/05").toFile(),
                StandardCharsets.UTF_8)),
            tempDir.resolve("logsDir/logs/some-logs/2024/05").toUri()
        );
        readers.put(
            new BufferedReader(new FileReader(tempDir.resolve("logsDir/logs/some-logs/05-2023.txt").toFile(),
                StandardCharsets.UTF_8)),
            tempDir.resolve("logsDir/logs/some-logs/05-2023.txt").toUri()
        );

        logAnalyzer = new LogAnalyzer(logMatcherDate, logMatcherFilter, reportMapper, logReport, readers);

        lenient().when(logMatcherDate.isLogMatch(any(LocalDateTime.class), eq(parameters))).thenReturn(false);
        lenient().when(logMatcherFilter.isLogMatchByFilter(any(Log.class), eq(parameters))).thenReturn(false);

        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.parse(anyString())).thenReturn(new Log(
                "ip",
                "user",
                LocalDateTime.now(),
                null,
                0,
                0,
                "field",
                "value"
            ));

            String result = logAnalyzer.analyzeLogs(parameters);
            String logError = outputStream.toString().trim();

            verify(logReport, times(0)).addAllStats(any(), anyString());

            assertThat(result).isEmpty();
            assertThat(logError).contains("Во входных файлах/URL не найдено ни одного лога.");
        }
    }

    @DisplayName("Тест чтения логов из URL")
    @Test
    void testAnalyzeLogsFromUrl() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        wireMockServer.stubFor(get(urlEqualTo("/test-logs"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody("log entry")));

        URI logUri = new URI("http://localhost:" + wireMockServer.port() + "/test-logs");
        BufferedReader urlReader = new BufferedReader(new InputStreamReader(
            new URI(logUri.toString()).toURL().openStream()));

        readers.put(urlReader, logUri);

        logAnalyzer = new LogAnalyzer(logMatcherDate, logMatcherFilter, reportMapper, logReport, readers);

        lenient().when(logMatcherDate.isLogMatch(any(LocalDateTime.class), eq(parameters))).thenReturn(true);
        lenient().when(logMatcherFilter.isLogMatchByFilter(any(Log.class), eq(parameters))).thenReturn(true);

        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.parse(anyString())).thenReturn(new Log(
                "ip",
                "user",
                LocalDateTime.now(),
                null,
                0,
                0,
                "field",
                "value"
            ));

            logAnalyzer.analyzeLogs(parameters);

            verify(logReport, times(1)).addAllStats(any(), eq(logUri.toString()));

            wireMockServer.verify(getRequestedFor(urlEqualTo("/test-logs")));
        }

        wireMockServer.stop();
    }

    @DisplayName("Тест чтения логов из URL, если не подошел ни один лог")
    @Test
    void testAnalyzeLogsFromUrlIfNoMatchLog() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        wireMockServer.stubFor(get(urlEqualTo("/test-logs"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody("log entry")));

        URI logUri = new URI("http://localhost:" + wireMockServer.port() + "/test-logs");
        BufferedReader urlReader = new BufferedReader(new InputStreamReader(
            new URI(logUri.toString()).toURL().openStream()));

        readers.put(urlReader, logUri);

        logAnalyzer = new LogAnalyzer(logMatcherDate, logMatcherFilter, reportMapper, logReport, readers);

        lenient().when(logMatcherDate.isLogMatch(any(LocalDateTime.class), eq(parameters))).thenReturn(false);
        lenient().when(logMatcherFilter.isLogMatchByFilter(any(Log.class), eq(parameters))).thenReturn(false);

        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.parse(anyString())).thenReturn(new Log(
                "ip",
                "user",
                LocalDateTime.now(),
                null,
                0,
                0,
                "field",
                "value"
            ));

            String result = logAnalyzer.analyzeLogs(parameters);
            String logError = outputStream.toString().trim();

            verify(logReport, times(0)).addAllStats(any(), eq(logUri.toString()));

            wireMockServer.verify(getRequestedFor(urlEqualTo("/test-logs")));

            assertThat(result).isEmpty();
            assertThat(logError).contains("Во входных файлах/URL не найдено ни одного лога.");
        }

        wireMockServer.stop();
    }

}
