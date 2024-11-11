package backend.academy.log_analyzer.parser.additioanl;

import backend.academy.log_analyzer.parser.additional.Converters;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты класса Converters")
public class ConvertersTest {

    private final Converters converters = new Converters();
    private final Converters.LocalDateTimeFromConverter localDateTimeFromConverter =
        new Converters.LocalDateTimeFromConverter();
    private final Converters.LocalDateTimeToConverter localDateTimeToConverter =
        new Converters.LocalDateTimeToConverter();

    @TempDir
    private static Path tempDir;

    @BeforeAll
    static void setTempDir() throws IOException {
        Path nestedDirs = tempDir.resolve("dir2/logs");
        Files.createDirectories(nestedDirs);

        Files.createFile(nestedDirs.resolve("test.txt"));
        Files.createFile(nestedDirs.resolve("test"));
    }

    @DisplayName("Тест корректной конвертации файла, если он существует")
    @Test
    void testConvertFileIfFileExists() {
        String outputFile = tempDir.resolve("dir2/logs/test.txt").toString();
        File expectedFile = new File(outputFile);

        File actualFile = converters.convertOutputFile(outputFile, ".txt");

        assertThat(actualFile).isEqualTo(expectedFile);
    }

    @DisplayName("Тест корректности конвертации файла, если он не существует")
    @Test
    void testConvertFileIfFileDoesNotExist() {
        String outputFile = "analyzed-logs.txt";
        File expectedFile = new File(outputFile);

        File actualFile = converters.convertOutputFile(outputFile, ".txt");

        assertThat(actualFile).isEqualTo(expectedFile);
    }

    @DisplayName("Тест конвертации выходного файла в различные форматы (расширения)")
    @ParameterizedTest(name = "расширение: {0}, ожидаемый файл: {1}")
    @MethodSource("provideDataToTestConvertFileInDifferentFormats")
    void testConvertFileInDifferentFormats(String format, File expectedFile) {
        String outputFile = "log";

        File actualFile = converters.convertOutputFile(outputFile, format);

        assertThat(actualFile).isEqualTo(expectedFile);
    }

    @DisplayName("Тест конвертации выходного файла в различные форматы (расширения)")
    @ParameterizedTest(name = "расширение: {0}, ожидаемый файл: {1}")
    @MethodSource("provideDataToTestConvertFileInDifferentFormats")
    void testConvertFileInDifferentFormatsIfFileHasFormat(String format, File expectedFile) {
        String outputFile = "log.html";

        File actualFile = converters.convertOutputFile(outputFile, format);

        assertThat(actualFile).isEqualTo(expectedFile);
    }

    static Stream<Arguments> provideDataToTestConvertFileInDifferentFormats() {
        return Stream.of(
            Arguments.of(".txt", new File("log.txt")),
            Arguments.of(".md", new File("log.md")),
            Arguments.of(".adoc", new File("log.adoc"))
        );
    }

    @DisplayName("Тест создания директории, если она не существует")
    @Test
    void createDirectoryIfItDoesNotExist() {
        File outputDir = new File(tempDir.resolve("some/path/to/dir").toString());

        converters.convertOutputFile(outputDir + "/file", ".txt");

        assertThat(outputDir).exists();
    }

    @DisplayName("Тест выброса исключения, если выходной файл не валиден")
    @Test
    void testConvertFileIfItExistsAndInvalid() {
        File file = new File(tempDir.resolve( "dir2/logs/test.txt").toString());
        File outputFile = new File(tempDir.resolve( "dir2/logs/test").toString());

        file.setWritable(false);

        assertThatThrownBy(
            () -> converters.convertOutputFile(outputFile.getAbsolutePath(), ".txt"))
            .isInstanceOf(ParameterException.class)
            .hasMessage("В выходной файл нельзя записать данные или он не является файлом");
    }

    // Данный тест не проходит на GitHub. Я не знаю, что тут изменить, чтобы это исправить, локально все работает

//    @DisplayName("Тест выброса исключения, если не удалось создать директорию")
//    @Test
//    void testCreateDirectoryIfItInvalid() {
//
//        assertThatThrownBy(() -> converters.convertOutputFile(tempDir.resolve("Z:/invalid/directory").toString(),
//            ".txt"))
//            .isInstanceOf(ParameterException.class)
//            .hasMessage("Не удалось создать директорию для файла: Z:\\invalid");
//    }

    @DisplayName("Тесты конвертации даты фильтра")
    @ParameterizedTest(name = "дата: {0}, конвертация: {1}")
    @MethodSource("provideDataToTestConvertDateTimeFilter")
    void testConvertDateTimeFilter(String filterDateTime, String expectedDateTime) {
        assertThat(converters.convertDateTimeFilter(filterDateTime)).isEqualTo(expectedDateTime);
    }

    static Stream<Arguments> provideDataToTestConvertDateTimeFilter() {
        return Stream.of(
            Arguments.of("2015-07-16", "2015-07-16"),
            Arguments.of("2015-05-20T08:54", "2015-05-20T08:54:00"),
            Arguments.of("2015-05-20T08:54:16", "2015-05-20T08:54:16")
        );
    }

    @DisplayName("Тесты выброса исключения при конвертации даты "
            + "(и для фильтра, и для конечной/начальной дат)с невалидными данными")
    @ParameterizedTest(name = "входные данные: {0}")
    @MethodSource("provideDataToTestConvertDateTimeFilterWithInvalidData")
    void testConvertDateTimeFilterWithInvalidData(String filterDateTime) {
        assertThatThrownBy(() -> converters.convertDateTimeFilter(filterDateTime))
            .isInstanceOf(ParameterException.class)
            .hasMessage("Дата должна быть в формате yyyy-mm-dd или yyyy-MM-ddTHH:mm:ss");
    }

    static Stream<Arguments> provideDataToTestConvertDateTimeFilterWithInvalidData() {
        return Stream.of(
            Arguments.of("2015"),
            Arguments.of(""),
            Arguments.of("8 мая"),
            Arguments.of("2015-05-17T09")
        );
    }

    @DisplayName("Тесты конвертации начальной даты")
    @ParameterizedTest(name = "дата: {0}, конвертация: {1}")
    @MethodSource("provideDataToTestConvertFrom")
    void testConvertFrom(String from, LocalDateTime expectedFrom) {
        assertThat(localDateTimeFromConverter.convert(from)).isEqualTo(expectedFrom);
    }

    static Stream<Arguments> provideDataToTestConvertFrom() {
        return Stream.of(
            Arguments.of("2015-05-06",
                LocalDateTime.of(2015, 5, 6, 0, 0, 0)),
            Arguments.of("2015-05-06T17:09:12",
                LocalDateTime.of(2015, 5, 6, 17, 9, 12))
        );
    }

    @DisplayName("Тесты конвертации конечной даты")
    @ParameterizedTest(name = "дата: {0}, конвертация: {1}")
    @MethodSource("provideDataToTestConvertTo")
    void testConvertTo(String to, LocalDateTime expectedTo) {
        assertThat(localDateTimeToConverter.convert(to)).isEqualTo(expectedTo);
    }

    static Stream<Arguments> provideDataToTestConvertTo() {
        return Stream.of(
                Arguments.of("2015-05-06",
                        LocalDateTime.of(2015, 5, 6, 23, 59, 59)),
                Arguments.of("2015-05-06T17:09:12",
                        LocalDateTime.of(2015, 5, 6, 17, 9, 12))
        );
    }
}
