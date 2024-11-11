package backend.academy.log_analyzer.factory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.assertThat;

// Учитывая, что в фабрику попадают провалидированные пути, исключений не должна выкидывать
@DisplayName("Тесты фабрики ридеров ReaderFactory")
public class ReaderFactoryTest {

    @TempDir
    static Path tempDir;

    Map<BufferedReader, URI> actualReaders;

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

        Files.createFile(dir1.resolve( "logs.txt"));

        Files.createFile(dir2.resolve( "05-2023.txt"));
        Files.createFile(dir2.resolve("logs-from-url.txt"));
        Files.createFile(dir2.resolve("logs_2024.log"));

        Files.createFile(dir3.resolve("05"));
        Files.createFile(dir3.resolve("05.log"));

        Files.createFile(dir4.resolve("logs"));

        Path sourceFile1 = Paths.get("src/main/resources/logs/logs.txt");
        Path sourceFile2 = Paths.get("src/main/resources/logs/some-logs/05-2023.txt");
        Path sourceFile3 = Paths.get("src/main/resources/logs/some-logs/logs-from-url.txt");
        Path sourceFile4 = Paths.get("src/main/resources/logs/some-logs/logs_2024.log");
        Path sourceFile5 = Paths.get("src/main/resources/logs/some-logs/2024/05");
        Path sourceFile6 = Paths.get("src/main/resources/logs/some-logs/2024/05.log");
        Path sourceFile7 = Paths.get("src/main/resources/logs/some-logs/2024_08/logs");

        Files.copy(sourceFile1, dir1.resolve("logs.txt"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile2, dir2.resolve("05-2023.txt"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile3, dir2.resolve("logs-from-url.txt"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile4, dir2.resolve("logs_2024.log"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile5, dir3.resolve("05"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile6, dir3.resolve("05.log"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile7, dir4.resolve("logs"), StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    void closeReaders() {
        actualReaders.keySet().forEach(r -> {
            try {
                r.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @DisplayName("Тест создания маппы ридеров для нескольких файлов")
    @Test
    void testCreateReadersForManyFiles() throws IOException {
        List<URI> paths = List.of(
                tempDir.resolve("logsDir/logs/logs.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/05-2023.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/logs-from-url.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/logs_2024.log").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024/05").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024/05.log").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024_08/logs").toUri()
        );

        actualReaders = ReaderFactory.createReaders(paths);

        Collection<URI> actualUris = actualReaders.values();

        assertThat(actualUris).hasSize(paths.size()).containsAll(paths);
    }

    @DisplayName("Тест корректности создания маппы ридеров для нескольких файлов с пропусками")
    @Test
    void testCreateReadersForManyFilesWithSkip() throws IOException {
        List<URI> paths = List.of(
                tempDir.resolve("logsDir/logs/logs.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/05-2023.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/logs-from-urld.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/logs_2024.log").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024/05d").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024/05.log").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024_08/logs").toUri()
        );

        List<URI> expectedUris = List.of(
                tempDir.resolve("logsDir/logs/logs.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/05-2023.txt").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/logs_2024.log").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024/05.log").toUri(),
                tempDir.resolve("logsDir/logs/some-logs/2024_08/logs").toUri()
        );

        actualReaders = ReaderFactory.createReaders(paths);

        Collection<URI> actualUris = actualReaders.values();

        assertThat(actualUris).hasSize(expectedUris.size()).containsAll(expectedUris);
    }
}
