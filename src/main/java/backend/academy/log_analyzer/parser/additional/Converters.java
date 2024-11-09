package backend.academy.log_analyzer.parser.additional;

import backend.academy.log_analyzer.enums.OutputFormats;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

// Я не знаю как избавиться от спотбагов, если это необходимо, подскажите, пожалуйста
/**
 * Класс для конвертации аргументов командной строки в параметры приложения
 * {@link backend.academy.log_analyzer.parameter.ArgsParameters}
 */
@SuppressFBWarnings(value = {"PATH_TRAVERSAL_IN"})
public class Converters {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String DATE_CONVERT_MESSAGE = "Дата должна быть в формате yyyy-mm-dd или yyyy-MM-ddTHH:mm:ss";

    /**
     * Метод конвертации выходного файла и последующей валидации сконвертированного файла
     *
     * @param outputFile выходной файл
     * @param format формат вывода
     *
     * @return провалидированный сконвертированный выходной файл
     *
     * @throws ParameterException если в выходной файл (сконвертированный)
     * нельзя записать данные или он не является файлом
     */
    public File convertOutputFile(String outputFile, String format) throws ParameterException {

        String convertedFileStr = outputFile.replace('/', File.separatorChar);

        String directory = "";

        try {
            directory = convertedFileStr.substring(0, convertedFileStr.lastIndexOf(File.separator));
        } catch (StringIndexOutOfBoundsException ignore) {
        }

        File convertedOutputFile = convertFile(format, directory, convertedFileStr);

        // Если изначально на вход подали файл без расширения, то оно устанавливается здесь и это уже новый файл.
        // Соответственно нужно провести валидацию. Если файл подали с расширением, то не надо - он кже провалидирован
        if (!outputFile.contains(".")) {
            if (convertedOutputFile.exists() && (!convertedOutputFile.isFile() || !convertedOutputFile.canWrite())) {
                throw new ParameterException("В выходной файл нельзя записать данные или он не является файлом");
            }
        }

        return convertedOutputFile;
    }

    /**
     * Метод для конвертации выходного файла
     *
     * @param format формат вывода
     * @param directory директория, где находится файл
     * @param convertedFileStr строка пути сконвертированного файла
     *
     * @return сконвертированный выходной файл
     */
    private File convertFile(String format, String directory, String convertedFileStr) {
        createDirectory(directory);

        String path = convertedFileStr.substring(0, convertedFileStr.contains(String.valueOf('.'))
            ? convertedFileStr.lastIndexOf('.')
            : convertedFileStr.length());

        String convertedPath = path + format;

        return new File(convertedPath);
    }

    /**
     * Метод для создания директории, где будет находиться выходной файл, если ее нет
     *
     * @param directory директория
     */
    private void createDirectory(String directory) {
        if (!directory.isEmpty()) {
            File directoryFile = new File(directory);

            if (!directoryFile.exists() && !directoryFile.mkdirs()) {
                throw new ParameterException("Не удалось создать директорию для файла: " + directory);
            }
        }
    }

    public String convertDateTimeFilter(String filterValue) {
        if (filterValue.contains("T")) {
            return convertDateTime(filterValue, "").format(FORMATTER);
        }

        try {
            return LocalDate.parse(filterValue, DateTimeFormatter.ISO_LOCAL_DATE).toString();
        } catch (DateTimeParseException e) {
            throw new ParameterException(DATE_CONVERT_MESSAGE, e);
        }
    }

    /**
     * Метод парсинга даты и времени
     *
     * @param dateTime дата и время
     * @param time время, которое ставится, если указана только дата. Начинается с 'T'
     *
     * @return дату и время в формате ISO
     */
    private static LocalDateTime convertDateTime(String dateTime, String time) {
        try {
            if (dateTime.contains("T")) {
                return LocalDateTime.parse(dateTime, FORMATTER);
            }

            return LocalDateTime.parse(dateTime + time, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ParameterException(DATE_CONVERT_MESSAGE, e);
        }
    }

    /**
     * Класс для конвертации {@code String} в {@code LocalDateTime} для начального времени.
     * Используется в {@link backend.academy.log_analyzer.parser.ArgsParser}
     *
     * @see LocalDateTimeFromConverter#convert(String)
     */
    public static class LocalDateTimeFromConverter implements IStringConverter<LocalDateTime> {

        /**
         * Метод конвертации строки в начальную дату.
         * В случае отсутствия в строке информации о времени, автоматически выставляется время {@code 00:00:00}
         *
         * @throws ParameterException если произошла ошибка парсинга из строки
         */
        @Override
        public LocalDateTime convert(String value) throws ParameterException {
            return convertDateTime(value, "T00:00:00");
        }
    }

    /**
     * Класс для конвертации {@code String} в {@code LocalDateTime} для конечного времени.
     * Используется в {@link backend.academy.log_analyzer.parser.ArgsParser}
     *
     * @see LocalDateTimeToConverter#convert(String)
     */
    public static class LocalDateTimeToConverter implements IStringConverter<LocalDateTime> {

        /**
         * Метод конвертации строки в конечную дату.
         * В случае отсутствия в строке информации о времени, автоматически выставляется время {@code 23:59:59}
         *
         * @throws ParameterException если произошла ошибка парсинга из строки
         */
        @Override
        public LocalDateTime convert(String value) throws ParameterException {
            return convertDateTime(value, "T23:59:59");
        }
    }

    //Логика простая, без тестов
    /**
     * Класс для конвертации формата (расширения) для вывода отчета.
     * Используется в {@link backend.academy.log_analyzer.parser.ArgsParser}
     *
     * @see OutputFormatConverter#convert(String)
     */
    public static class OutputFormatConverter implements IStringConverter<String> {

        /**
         * Метод конвертации названия формата для вывода в расширение данного формата
         */
        @Override
        public String convert(String format) {
            return OutputFormats.valueOf(format.toUpperCase()).extension();
        }
    }
}
