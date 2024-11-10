package backend.academy.log_analyzer.parser;

import backend.academy.log_analyzer.enums.OutputFormats;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import backend.academy.log_analyzer.parser.additional.Converters;
import backend.academy.log_analyzer.parser.additional.Validators;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// Тестов на выброс исключений, правильной конвертации не будет - уже протестировано для соответствующих классов
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты класса ArgsParametersParser")
public class ArgsParametersParserTest {

    @Spy
    ArgsParser argsParser = new ArgsParser();

    @Mock
    PathParser pathParser;

    @Mock
    Validators validators;

    @Mock
    Converters converters;

    @InjectMocks
    ArgsParametersParser argsParametersParser;

    @BeforeEach
    void setUpBeforeClass() throws URISyntaxException {
        when(pathParser.parsePath(anyString())).thenReturn(List.of(new URI("inputFile.txt")));
        when(converters.convertOutputFile(anyString(), anyString())).thenReturn(new File("outputFile.txt"));
    }

    @DisplayName("Тест парсинга параметров")
    @Test
    void testParseParameters() throws URISyntaxException {

        String[] args = {
            "-p", "inputFile.txt",
            "-fe",
            "-o", "outputFile.txt",
            "-f", "2015-05-07T20:00:00",
            "-t", "2015-05-08T20:00:00",
            "-ft", "text",
            "-ff", "method",
            "-fv", "GET"
        };

        ArgsParameters expectedArgsParameters = new ArgsParameters(
            List.of(new URI("inputFile.txt")),
            true,
            new File("outputFile.txt"),
            LocalDateTime.of(2015, 5, 7, 20, 0, 0),
            LocalDateTime.of(2015, 5, 8, 20, 0, 0),
            OutputFormats.TEXT,
            "method",
            "GET"
        );

        ArgsParameters actualArgsParameters = argsParametersParser.parse(args);

        assertThat(actualArgsParameters).isEqualTo(expectedArgsParameters);
    }

    @DisplayName("Тест принудительной установки вывода в файл")
    @Test
    void testParseParametersInFile() throws URISyntaxException {
        String[] args = {
            "-p", "inputFile.txt",
            "-o", "outputFile.txt",
        };

        ArgsParameters expectedArgsParameters = new ArgsParameters(
            List.of(
                new URI("inputFile.txt")),
            true,
            new File("outputFile.txt"),
            null,
            null,
            OutputFormats.TEXT,
            "",
            ""
        );

        ArgsParameters actualArgsParameters = argsParametersParser.parse(args);

        assertThat(actualArgsParameters).isEqualTo(expectedArgsParameters);
    }

    @DisplayName("Тест замены формата вывода при указании расширения файла")
    @Test
    void testParseParametersWithFileExtension() throws URISyntaxException {
        String[] args = {
            "-p", "inputFile.txt",
            "-o", "outputFile.md",
            "-ft", "adoc"
        };

        ArgsParameters expectedArgsParameters = new ArgsParameters(
            List.of(new URI("inputFile.txt")),
            true,
            new File("outputFile.txt"),
            null,
            null,
            OutputFormats.MARKDOWN,
            "",
            ""
        );

        ArgsParameters actualArgsParameters = argsParametersParser.parse(args);

        assertThat(actualArgsParameters).isEqualTo(expectedArgsParameters);
    }
}
