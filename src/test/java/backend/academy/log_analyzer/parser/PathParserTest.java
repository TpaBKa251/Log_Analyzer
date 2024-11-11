package backend.academy.log_analyzer.parser;

import com.beust.jcommander.ParameterException;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class PathParserTest {

    PathParser pathParser = new PathParser();

    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 400;
    @TempDir
    static Path tempDir;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance().build();

    @BeforeAll
    static void setTempDir() throws IOException {
        Path nestedDir1 = tempDir.resolve("logsDir/logs/some-logs/2024");
        Path nestedDir2 = tempDir.resolve("logsDir/logs/some-logs/2024_08");

        Files.createDirectories(nestedDir1);
        Files.createDirectories(nestedDir2);

        Path dir1 = tempDir.resolve("logsDir/logs");
        Path dir2 = tempDir.resolve("logsDir/logs/some-logs");
        Path dir3 = tempDir.resolve("logsDir/logs/some-logs/2024");
        Path dir4 = tempDir.resolve("logsDir/logs/some-logs/2024_08");

        Files.createFile(dir1.resolve("logs.txt"));

        Files.createFile(dir2.resolve("05-2023.txt"));
        Files.createFile(dir2.resolve("logs-from-url.txt"));
        Files.createFile(dir2.resolve("logs_2024.txt"));

        Files.createFile(dir3.resolve("05"));
        Files.createFile(dir3.resolve("05.txt"));

        Files.createFile(dir4.resolve("logs"));

        Path sourceFile2 = Paths.get("src/main/resources/logs/some-logs/05-2023.txt");
        Path sourceFile4 = Paths.get("src/main/resources/logs/some-logs/logs_2024.txt");
        Path sourceFile5 = Paths.get("src/main/resources/logs/some-logs/2024/05");
        Path sourceFile6 = Paths.get("src/main/resources/logs/some-logs/2024/05.txt");
        Path sourceFile7 = Paths.get("src/main/resources/logs/some-logs/2024_08/logs");

        Files.copy(sourceFile2, dir2.resolve("05-2023.txt"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile4, dir2.resolve("logs_2024.txt"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile5, dir3.resolve("05"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile6, dir3.resolve("05.txt"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile7, dir4.resolve("logs"), StandardCopyOption.REPLACE_EXISTING);
    }

    @BeforeEach
    void setUpUrl() {
        wireMock.resetAll();
    }

    @DisplayName("Тест успешного парсинга конкретного файла")
    @Test
    void testParseSingleFileSuccessful() {
        String path = tempDir.resolve("logsDir/logs/some-logs/2024/05.txt").toAbsolutePath().toString();
        path = path.replace("\\", "/");

        List<URI> expectedList = List.of(Path.of(path).toUri());

        List<URI> actualPath = pathParser.parsePath(path);

        assertThat(actualPath).isEqualTo(expectedList);
    }

    @DisplayName("Тест провального парсинга конкретного файла. Должен выкинуть исключение")
    @ParameterizedTest(name = "файл: {0}, ошибка: {1}")
    @MethodSource("provideDataToTestParseSingleFileFailed")
    void testParseSingleFileFailed(String invalidFile, String expectedMessage) {
        String path = tempDir.resolve(invalidFile).toAbsolutePath().toString();

        assertThatThrownBy(() -> pathParser.parsePath(path))
            .isInstanceOf(ParameterException.class)
            .hasMessageStartingWith(expectedMessage);
    }

    static Stream<Arguments> provideDataToTestParseSingleFileFailed() {
        return Stream.of(
            Arguments.of("notExists", "Некорректный путь или файл недоступен для чтения:"),
            Arguments.of("logsDir", "Некорректный путь или файл недоступен для чтения:"),
            Arguments.of("logsDir/logs/logs.txt", "Входной файл")
        );
    }

    @DisplayName("Тест успешного парсинга файлов по шаблону")
    @ParameterizedTest(name = "шаблон: {0}, ожидаемые файлы: {1}")
    @MethodSource("provideDataToTestParsePatternFilesSuccessful")
    void testParsePatternFilesSuccessful(String pattern, List<URI> expectedFiles) {
        assertThat(pathParser.parsePath(pattern)).containsExactlyElementsOf(expectedFiles);
    }

    static Stream<Arguments> provideDataToTestParsePatternFilesSuccessful() {
        return Stream.of(
            Arguments.of(tempDir.toFile() + "/**/05", List.of(
                tempDir.resolve("logsDir/logs/some-logs/2024/05").toUri())),
            Arguments.of(tempDir.toFile() + "/logsDir/logs/some-logs/*.txt", List.of(
                tempDir.resolve("logsDir/logs/some-logs/05-2023.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/logs_2024.txt").toUri()
            )),
            Arguments.of(tempDir.toFile() + "/logsDir/logs/some-logs/**", List.of(
                tempDir.resolve("logsDir/logs/some-logs/05-2023.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024/05").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024/05.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024_08/logs").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/logs_2024.txt").toUri()
            )),
            Arguments.of(tempDir.toFile() + "/logsDir/logs/**/logs*.txt", List.of(
                tempDir.resolve("logsDir/logs/some-logs/logs_2024.txt").toUri()
            ))
        );
    }

    @DisplayName("Тест провального парсинга файлов по шаблону. Должен выкинуть исключение")
    @Test
    void testParsePatternFilesFailed() {
        String pattern = tempDir.toFile() + "/logsDir/logs/**/logs*.log";

        assertThatThrownBy(() -> pathParser.parsePath(pattern))
            .isInstanceOf(ParameterException.class)
            .hasMessage("По шаблону " + pattern + " не найдено ни одного валидного файла");
    }

    @DisplayName("Тест успешного парсинга URL")
    @Test
    void testParseUrlSuccessful() throws Exception {
        String urlString = wireMock.url("/example");
        URI expectedUri = new URI(urlString);

        wireMock.stubFor(head(urlEqualTo("/example"))
            .willReturn(aResponse()
                .withStatus(SUCCESS_CODE)
                .withFixedDelay(0)));

        List<URI> result = pathParser.parsePath(urlString);

        assertThat(result).containsExactly(expectedUri);
    }

    @DisplayName("Тест провального парсинга URL. Должен выкинуть исключение")
    @Test
    void testParseUrlFailed() {
        String urlString = wireMock.url("/not-found");

        wireMock.stubFor(head(urlEqualTo("/not-found"))
            .willReturn(aResponse()
                .withStatus(ERROR_CODE)
                .withFixedDelay(0)));

        assertThatThrownBy(() -> pathParser.parsePath(urlString))
            .isInstanceOf(ParameterException.class)
            .hasMessage("URL " + urlString + " не доступна");
    }
}
