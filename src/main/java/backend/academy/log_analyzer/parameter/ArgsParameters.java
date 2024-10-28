package backend.academy.log_analyzer.parameter;

import backend.academy.log_analyzer.enums.OutputFormats;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Рекорд для хранения сконвертированных параметров, полученных с помощью аргументов командной строки
 *
 * @param path пути до ресурсов, откуда читаются логи
 * @param inFile нужно ли выводить результат в файл
 * @param outputFile выходной файл
 * @param from начальная дата
 * @param to конечная дата
 * @param format формат вывода
 * @param filterField поле для фильтрации
 * @param filterValue значение поля для фильтрации
 */
public record ArgsParameters(
    List<URI> path,
    boolean inFile,
    File outputFile,
    LocalDateTime from,
    LocalDateTime to,
    OutputFormats format,
    String filterField,
    String filterValue
) {
}
