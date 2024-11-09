package backend.academy.log_analyzer.enums;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты перечисления OutputFormats")
public class OutputFormatsTest {

    @DisplayName("Тест корректности создания элемента по расширению")
    @ParameterizedTest(name = "расширение: {0}, ожидаемый элемент: {1}")
    @MethodSource("provideDataToTestOutputFormatsNameOf")
    void testOutputFormatsNameOf(String extension, OutputFormats expectedOutputFormat) {
        OutputFormats actualOutputFormat = OutputFormats.of(extension);

        assertThat(actualOutputFormat).isEqualTo(expectedOutputFormat);
    }

    static Stream<Arguments> provideDataToTestOutputFormatsNameOf() {
        return Stream.of(
            Arguments.of(".md", OutputFormats.MARKDOWN),
            Arguments.of(".adoc", OutputFormats.ADOC),
            Arguments.of(".txt", OutputFormats.TEXT),
            Arguments.of(".html", null)
        );
    }
}
