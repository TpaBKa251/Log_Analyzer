package backend.academy.log_analyzer.parser.additional;

import backend.academy.log_analyzer.enums.OutputFormats;
import backend.academy.log_analyzer.util.LogFieldsStorage;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

// Я не знаю как избавиться от спотбагов (PATH_TRAVERSAL_IN), если это необходимо, подскажите, пожалуйста
// WOC_WRITE_ONLY_COLLECTION_FIELD - ложный

/**
 * Класс для валидации аргументов командной строки
 */
@SuppressFBWarnings(value = {"PATH_TRAVERSAL_IN", "WOC_WRITE_ONLY_COLLECTION_FIELD"})
@Slf4j
public class Validators {

    private static final List<String> FORMATS = Arrays.stream(OutputFormats.values())
            .map(OutputFormats::formatName)
            .toList();

    private static final String INVALID_DIRECTORY_MESSAGE = "Нельзя создать файл в указанной директории";

    /**
     * Метод валидации стартовой и конечной даты
     *
     * @param from стартовая дата
     * @param to   конечная дата
     * @throws ParameterException если стартовая дата в будущем или стартовая дата идет после конечной
     */
    public void validateDate(LocalDateTime from, LocalDateTime to) throws ParameterException {
        if (from != null) {
            if (from.isAfter(LocalDateTime.now())) {
                throw new ParameterException("Аргумент --from (-f) должен быть до текущего времени "
                        + LocalDateTime.now());
            }

            if (to != null && from.isAfter(to)) {
                throw new ParameterException("Аргумент --from (-f) по временной линии должен идти до --to (-t)");
            }
        }
    }

    // Простая логика - без тестов

    /**
     * Метод для валидации фильтра.
     *
     * @param filterField поле для фильтрации
     * @param filterValue значение поля для фильтрации
     * @throws ParameterException если нет одного из параметров: {@code filterField} или {@code filterValue}
     */
    public void validateFilter(String filterField, String filterValue) throws ParameterException {
        if (!filterField.isEmpty() && filterValue.isEmpty()
                || !filterValue.isEmpty() && filterField.isEmpty()
        ) {
            throw new ParameterException("При указании --filter-field (-ff) "
                    + "необходимо указать --filter-value (fv) и наоборот");
        }
    }

    // Простая логика - без тестов

    /**
     * Класс для валидации формата для вывода.
     * Используется в {@link backend.academy.log_analyzer.parser.ArgsParser}
     *
     * @see FormatValidator#validate(String, String)
     */
    public static class FormatValidator implements IParameterValidator {

        /**
         * Метод валидации формата для вывода
         *
         * @throws ParameterException если формат для вывода не совпадает с разрешенными {@link OutputFormats}
         */
        @Override
        public void validate(String name, String value) throws ParameterException {

            if (!FORMATS.contains(value)) {
                throw new ParameterException(
                        "Аргумент для " + name + " должен быть " + FORMATS + ". Найдено: " + value);
            }
        }
    }

    // Простая логика - без тестов

    /**
     * Класс для валидации поля для фильтрации
     * Используется в {@link backend.academy.log_analyzer.parser.ArgsParser}
     *
     * @see FilterFieldValidator#validate(String, String)
     */
    public static class FilterFieldValidator implements IParameterValidator {

        /**
         * Метод валидации поля для фильтрации
         *
         * @throws ParameterException если поле для фильтрации не совпадает с возможными параметрами в NGINX логе
         */
        @Override
        public void validate(String name, String fieldName) throws ParameterException {

            List<String> logFields = LogFieldsStorage.ALL_FIELDS;

            if (!logFields.contains(fieldName)) {
                throw new ParameterException("Указанного поля " + fieldName + " для фильтрации не существует. "
                        + "Используйте: " + logFields);
            }
        }
    }

    /**
     * Класс для валидации выходного файла
     * Используется в {@link backend.academy.log_analyzer.parser.ArgsParser}
     *
     * @see OutputFileValidator#validate(String, String)
     */
    public static class OutputFileValidator implements IParameterValidator {
        /**
         * Метод для валидации выходного файла
         *
         * @throws ParameterException <p>
         *                            если выходной файл в недопустимом расширении {@link OutputFormats}
         *                            <p>
         *                            если выходной файл не является файлом или в него нельзя записать данные
         *                            <p>
         *                            если нельзя создать файл в указанной директории
         */
        @Override
        public void validate(String name, String outputFile) throws ParameterException {
            validateExtension(outputFile);

            File file = new File(outputFile);
            File parentFile = file.getAbsoluteFile().getParentFile();

            String convertedOutputFile = outputFile.replace('/', File.separatorChar);

            String directory = "";
            File directoryFile = null;

            if (convertedOutputFile.endsWith(File.separator)) {
                try {
                    directory = convertedOutputFile.substring(0, convertedOutputFile.lastIndexOf(File.separator));
                    directoryFile = new File(directory);
                } catch (StringIndexOutOfBoundsException ignore) {
                }
            }

            /*
            Если подали директорию и она существует, то валидируем только ее,
            так как внутри создастся новый 100% валидный файл.
            Если подали файл и он существует и при этом с расширением, то валидируем только его.
            Почему важно наличие расширения: при его отсутствии в конвертере создастся файл с расширением,
            и этот файл будет отличен от того, который находится здесь,
            поэтому валидация файла без расширения не производится.
            В крайнем случае валидируем родительскую директорию файла или директории
            */
            if (!directory.isEmpty() && directoryFile != null && directoryFile.exists()) {
                validateDirectory(directoryFile);
            } else if (file.exists() && convertedOutputFile.contains(".")) {
                validateFile(file);
            } else {
                validateDirectory(parentFile);
            }
        }

        /**
         * Метод валидации расширения файла
         *
         * @param file файл для валидации
         */
        private void validateExtension(String file) {
            if (file.contains(".")
                    && OutputFormats.of(file.substring(file.lastIndexOf('.'))) == null) {
                throw new ParameterException("Неизвестное расширение файла: " + file
                        + " Допустимые расширения: " + FORMATS);
            }
        }

        /**
         * Метод для валидации файла
         *
         * @param file файл для валидации
         */
        private void validateFile(File file) {
            if (!file.isFile() || !file.canWrite()) {
                throw new ParameterException("Выходной файл не является файлом "
                        + "или в него нельзя записать данные");
            }
        }

        /**
         * Метод для валидации директории
         *
         * @param directory директория для валидации
         */
        private void validateDirectory(File directory) {
            if (directory != null && directory.exists() && (!directory.isDirectory() || !directory.canWrite())) {
                throw new ParameterException(INVALID_DIRECTORY_MESSAGE);
            }

            try {
                // ну такого файла на ПК точно не должно быть
                File testFile = new File(directory, "tempFileForValidationSoThisFileCannotExistIHopePlease");

                if (testFile.createNewFile()) {
                    if (!testFile.delete()) {
                        log.info("Я вам файл создал :) {}", testFile);
                    }
                } else {
                    throw new ParameterException(INVALID_DIRECTORY_MESSAGE);
                }
            } catch (IOException e) {
                throw new ParameterException(INVALID_DIRECTORY_MESSAGE, e);
            }
        }
    }

}
