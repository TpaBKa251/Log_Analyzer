package backend.academy.log_analyzer.parser;

import backend.academy.log_analyzer.enums.OutputFormats;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import backend.academy.log_analyzer.parser.additional.Converters;
import backend.academy.log_analyzer.parser.additional.Validators;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Inject;
import java.io.File;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Класс для парсинга и конвертации аргументов командной строки в параметры приложения {@link ArgsParameters}
 */
@Slf4j
public class ArgsParametersParser {

    private static final String DEFAULT_FILE_NAME = "analyzed-logs";

    private final ArgsParser argsParser;
    private final PathParser pathParser;
    private final Validators validators;
    private final Converters converters;

    @Inject
    public ArgsParametersParser(
        ArgsParser argsParser,
        PathParser pathParser,
        Validators validators,
        Converters converters
    ) {
        this.argsParser = argsParser;
        this.pathParser = pathParser;
        this.validators = validators;
        this.converters = converters;
    }

    /**
     * Метод для парсинга и конвертации аргументов командной строки
     *
     * @param args аргументы командной строки
     *
     * @return сконвертированные параметры приложения
     */
    public ArgsParameters parse(String[] args) {
        JCommander jCommander = JCommander.newBuilder()
            .addObject(argsParser)
            .build();

        jCommander.setProgramName("Анализатор логов");

        List<URI> path;
        File outputFile;
        boolean inFile;
        String format;
        String filterValue;

        try {
            jCommander.parse(args);

            filterValue = argsParser.filterValue();

            if ("time".equals(argsParser.filterField()) && !argsParser.filterValue().contains("*")) {
                filterValue = converters.convertDateTimeFilter(filterValue);
            }

            inFile = argsParser.inFile();
            format = argsParser.format();

            validators.validateDate(argsParser.from(), argsParser.to());
            validators.validateFilter(argsParser.filterField(), argsParser.filterValue());

            path = pathParser.parsePath(argsParser.path());

            String outputFileStr = argsParser.outputFile();

            /*
            Если пользователь не подал файл для вывода - просто задаем значение по умолчанию
            Если пользователь подал директорию, то добавляем к ней файл по умолчанию,
            а также ставим вывод в файл в true,
            так как пользователь указал файл для вывода - очевидно, что он хочет получить вывод туда,
            но мог забыть поставить флаг.
            Если файл задан, то ставим вывод в файл в true по причине выше.
             */
            if (outputFileStr == null) {
                outputFileStr = DEFAULT_FILE_NAME;
            } else if (outputFileStr.endsWith("/")) {
                outputFileStr += DEFAULT_FILE_NAME;
                inFile = true;
            } else {
                inFile = true;
            }

            // Если пользователь указал файл с расширением, то берем расширение файла в качестве формата вывода.
            // Если указали расширение файла - скорее всего в этом расширении (формате) и хотят результат получить
            if (outputFileStr.contains(".")) {
                format = outputFileStr.substring(outputFileStr.lastIndexOf('.'));
            }

            outputFile = converters.convertOutputFile(outputFileStr, format);
        } catch (ParameterException e) {
            jCommander.usage();
            throw e;
        }

        return new ArgsParameters(
            path,
            inFile,
            outputFile,
            argsParser.from(),
            argsParser.to(),
            OutputFormats.of(format),
            argsParser.filterField(),
            filterValue
        );
    }
}

