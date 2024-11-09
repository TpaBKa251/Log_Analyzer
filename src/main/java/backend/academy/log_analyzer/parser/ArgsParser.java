package backend.academy.log_analyzer.parser;

import backend.academy.log_analyzer.parser.additional.Converters;
import backend.academy.log_analyzer.parser.additional.Validators;
import com.beust.jcommander.Parameter;
import java.time.LocalDateTime;
import lombok.Getter;

// Тесты валидации и конвертации есть, тестировать функционал библиотеки нет необходимости
/**
 * Класс для парсинга аргументов командной строки
 */
@Getter
public class ArgsParser {

    @Parameter(names = {"--path", "-p"}, description = "Путь до файл(а)/ов или URL", required = true)
    private String path;

    @Parameter(names = {"--file", "-fe"}, description = "Включить вывод в файл")
    private boolean inFile = false;

    @Parameter(names = {"--out", "-o"}, description = "Путь до выходного файла",
        validateWith = Validators.OutputFileValidator.class)
    private String outputFile;

    @Parameter(names = {"--from", "-f"}, description = "Стартовые дата и время в формате ISO8601",
        converter = Converters.LocalDateTimeFromConverter.class)
    private LocalDateTime from;

    @Parameter(names = {"--to", "-t"}, description = "Конечные дата и время в формате ISO8601",
        converter = Converters.LocalDateTimeToConverter.class)
    private LocalDateTime to;

    @Parameter(names = {"--format", "-ft"}, description = "Формат вывода",
        validateWith = Validators.FormatValidator.class, converter = Converters.OutputFormatConverter.class)
    private String format = ".txt";

    @Parameter(names = {"--filter-field", "-ff"}, description = "Поле для фильтрации",
        validateWith = Validators.FilterFieldValidator.class)
    private String filterField = "";

    @Parameter(names = {"--filter-value", "-fv"}, description = "Значение поля для фильтрации")
    private String filterValue = "";
}
