package backend.academy.log_analyzer.mathcer;

import backend.academy.log_analyzer.enums.OutputFormats;
import backend.academy.log_analyzer.matcher.LogMatcherDate;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты класс LogMatcherDate")
public class LogMatcherDateTest {

    LogMatcherDate logMatcherDate = new LogMatcherDate();

    @DisplayName("Тесты корректной фильтрации по дате")
    @ParameterizedTest(name = "начальная дата: {0}, конечная дата: {1}, время лога: {2}, ожидаемый результат: {3}")
    @MethodSource("provideDataToTestIsLogMatchByDate")
    void testIsLogMatchByDate(LocalDateTime from, LocalDateTime to, LocalDateTime logTime, boolean expectedMatch) {
        ArgsParameters argsParameters = new ArgsParameters(
                List.of(),
                false,
                new File("hello"),
                from,
                to,
                OutputFormats.TEXT,
                "",
                ""
        );
        assertThat(logMatcherDate.isLogMatch(logTime, argsParameters)).isEqualTo(expectedMatch);
    }

    static Stream<Arguments> provideDataToTestIsLogMatchByDate() {
        return Stream.of(
                Arguments.of(null, null, LocalDateTime.now(), true),
                Arguments.of(
                        null,
                        LocalDateTime.of(2015, 5, 17, 23, 59, 59),
                        LocalDateTime.of(2015, 5, 17, 23, 59, 59),
                        true
                ),
                Arguments.of(
                        null,
                        LocalDateTime.of(2015, 5, 17, 22, 10, 16),
                        LocalDateTime.of(2015, 4, 17, 0, 0, 0),
                        true
                ),
                Arguments.of(
                        null,
                        LocalDateTime.of(2015, 5, 17, 12, 12, 12),
                        LocalDateTime.of(2015, 5, 17, 12, 12, 13),
                        false
                ),
                Arguments.of(
                        LocalDateTime.of(2015, 5, 17, 0, 0),
                        null,
                        LocalDateTime.of(2015, 5, 17, 0, 0),
                        true
                ),
                Arguments.of(
                        LocalDateTime.of(2015, 5, 17, 10, 15, 10),
                        LocalDateTime.of(2015, 5, 17, 11, 15, 10),
                        LocalDateTime.of(2015, 5, 17, 11, 15, 10),
                        true
                ),
                Arguments.of(
                        LocalDateTime.of(2015, 5, 17, 0, 0),
                        null,
                        LocalDateTime.of(2015, 5, 16, 0, 0),
                        false
                ),
                Arguments.of(
                        LocalDateTime.of(2015, 5, 17, 0, 0),
                        LocalDateTime.of(2015, 5, 17, 0, 0),
                        LocalDateTime.of(2015, 5, 17, 0, 0),
                        true
                ),
                Arguments.of(
                        LocalDateTime.of(2015, 5, 17, 10, 12, 10),
                        LocalDateTime.of(2015, 5, 17, 22, 56, 14),
                        LocalDateTime.of(2015, 5, 17, 13, 0, 0),
                        true
                ),
                Arguments.of(
                        LocalDateTime.of(2015, 5, 17, 12, 10),
                        LocalDateTime.of(2015, 6, 17, 0, 0),
                        LocalDateTime.of(2015,4, 15, 10, 10),
                        false
                ),
                Arguments.of(
                        LocalDateTime.of(2015, 5, 17, 12, 10),
                        LocalDateTime.of(2015, 6, 17, 0, 0),
                        LocalDateTime.of(2015,7, 15, 10, 10),
                        false
                )
        );
    }
}
