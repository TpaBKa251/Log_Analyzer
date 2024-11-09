package backend.academy.log_analyzer.enums;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты перечисления HttpCodes")
public class HttpCodesTest {

    @DisplayName("Тест корректности создания элемента по коду")
    @ParameterizedTest(name = "код = {0}, ожидаемый элемент: {1}")
    @MethodSource("provideDataToTestHttpCodesOf")
    void testHttpCodesOf(int code, HttpCodes expectedHttpCode) {
        HttpCodes actualHttpCode = HttpCodes.of(code);

        assertThat(actualHttpCode).isEqualTo(expectedHttpCode);
    }

    static Stream<Arguments> provideDataToTestHttpCodesOf() {
        return Stream.of(
            Arguments.of(200, HttpCodes.OK),
            Arguments.of(404, HttpCodes.NOT_FOUND),
            Arguments.of(123, HttpCodes.UNKNOWN)
        );
    }
}
