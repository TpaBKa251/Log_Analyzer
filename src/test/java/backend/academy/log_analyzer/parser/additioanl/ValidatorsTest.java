package backend.academy.log_analyzer.parser.additioanl;

import backend.academy.log_analyzer.parser.additional.Validators;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@DisplayName("Тесты класса Validators")
public class ValidatorsTest {

    @TempDir
    Path tempDir;

    Validators validators = new Validators();
    Validators.OutputFileValidator outputFileValidator = new Validators.OutputFileValidator();

    @BeforeEach
    void setTempDir() throws IOException {
        Path nestedDirs = tempDir.resolve("dir2/logs");
        Files.createDirectories(nestedDirs);

        Files.createFile(nestedDirs.resolve("test.txt"));
        Files.createFile(nestedDirs.resolve("test"));
        Files.createFile(nestedDirs.resolve("test.html"));
    }

    @DisplayName("Тесты валидации начальной и конечной дат с валидными данными")
    @Test
    void estValidateDateWithValidData() {
        LocalDateTime from = LocalDateTime.MIN;
        LocalDateTime to = LocalDateTime.now();

        assertThatNoException().isThrownBy(() -> validators.validateDate(from, to));
    }

    @DisplayName("Тесты валидации начальной и конечной дат с невалидными данными")
    @ParameterizedTest(name = "начальная дата: {0}, конечная дата: {1}, ожидаемое сообщение: {2}")
    @MethodSource("provideDataToTestValidateDateWithInvalidData")
    void testValidateDateWithInvalidData(LocalDateTime from, LocalDateTime to, String expectedMessage) {
        assertThatThrownBy(() -> validators.validateDate(from, to))
                .isInstanceOf(ParameterException.class)
                .hasMessageStartingWith(expectedMessage);
    }

    static Stream<Arguments> provideDataToTestValidateDateWithInvalidData() {
        return Stream.of(
                Arguments.of(
                        LocalDateTime.MAX,
                        null,
                        "Аргумент --from (-f) должен быть до текущего времени"
                ),

                Arguments.of(
                        LocalDateTime.now(),
                        LocalDateTime.now().minusDays(1),
                        "Аргумент --from (-f) по временной линии должен идти до --to (-t)"
                )
        );
    }

    @DisplayName("Тесты валидации выходного файла или директории, если они существуют с валидными данными")
    @ParameterizedTest(name = "данные: {0}, должна успешно пройти валидация: {1}") // {1} просто для пояснения
    @MethodSource("provideDataToTestValidateFileOrDirectoryIfItExistWithValidData")
    void testValidateFileOrDirectoryIfItExistWithValidData(String outputFileOrDirectory, String validations) {
        String file = tempDir.resolve(outputFileOrDirectory).toFile().getAbsoluteFile().toString();
        assertThatNoException().isThrownBy(() -> outputFileValidator.validate("out", file));
    }

    static Stream<Arguments> provideDataToTestValidateFileOrDirectoryIfItExistWithValidData() {
        return Stream.of(
                Arguments.of("dir2/logs/test.txt", "расширения, файла"), // файл с расширением
                Arguments.of("dir2/logs/test", "родительской директории"), // файл без расширения
                Arguments.of("dir2/", "поданной директории") // директория
        );
    }

    @DisplayName("Тесты валидации выходного файла или директории, если они НЕ существуют с валидными данными")
    @ParameterizedTest(name = "данные: {0}, должна успешно пройти валидация: {1}") // {1} просто для пояснения
    @MethodSource("provideDataToTestValidateDirectoryIfItExistWithValidData")
    void testValidateFileOrDirectoryIfItDoNotExistWithValidData(String outputFileOrDirectory, String validations) {
        assertThatNoException().isThrownBy(() -> outputFileValidator.validate("out", outputFileOrDirectory));
    }

    static Stream<Arguments> provideDataToTestValidateDirectoryIfItExistWithValidData() {
        return Stream.of(
                Arguments.of("log.md", "расширения, родительской директории"),
                Arguments.of("logs", "родительской директории"),
                Arguments.of("logs/", "родительской директории")
        );
    }

    @DisplayName("Тесты валидации выходного файла, если он существует с НЕвалидными данными")
    @ParameterizedTest(name = "данные: {0}, ошибка: {1}")
    @MethodSource("provideDataToTestValidateFileIfItExistWithInvalidData")
    void testValidateFileIfItExistWithInvalidData(String outputFile, String expectedMessage) {
        File file = new File(tempDir.resolve(outputFile).toString());

        file.setReadOnly();

        assertThatThrownBy(() -> outputFileValidator.validate("out", file.getAbsolutePath()))
                .isInstanceOf(ParameterException.class)
                .hasMessageStartingWith(expectedMessage);
    }

    static Stream<Arguments> provideDataToTestValidateFileIfItExistWithInvalidData() {
        return Stream.of(
                Arguments.of("dir2/logs/test.html", "Неизвестное расширение файла"),
                Arguments.of("dir2/logs/test.txt", "Выходной файл не является файлом")
        );
    }

    @DisplayName("Тесты валидации выходного файла, если он НЕ существует с НЕвалидными данными")
    @Test()
    void testValidateFileIfItDoNotExistWithInvalidData() {
        File file = new File(tempDir.resolve("log.html").toString());

        assertThatThrownBy(() -> outputFileValidator.validate("out", file.getAbsolutePath()))
                .isInstanceOf(ParameterException.class)
                .hasMessageStartingWith("Неизвестное расширение файла");
    }
}
