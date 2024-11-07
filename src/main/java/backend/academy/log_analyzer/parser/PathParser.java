package backend.academy.log_analyzer.parser;

import com.beust.jcommander.ParameterException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// Я не знаю как избавиться от спотбагов, если это необходимо, подскажите, пожалуйста
/**
 * Класс для парсинга входного пути, URL, шаблона
 */
@SuppressFBWarnings(value = {"URLCONNECTION_SSRF_FD", "PATH_TRAVERSAL_IN"})
public class PathParser {

    private static final int TIMEOUT = 5000;
    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 400;

    /**
     * Метод для парсинга входного пути, URL, шаблона
     *
     * @param path путь, URL, шаблон
     *
     * @return список путей до всех ресурсов
     */
    public List<URI> parsePath(String path) {
        try {
            if (isUrl(path)) {
                return parseUrl(path);
            } else if (containsWildcard(path)) {
                return parsePatternFiles(path);
            }

            return parseSingleFile(path);
        } catch (IOException e) {
            throw new ParameterException(e);
        }
    }

    /**
     * Метод для проверки является ли входной путь URL
     *
     * @param path путь
     *
     * @return {@code true}, если путь является URL, иначе - {@code false}
     */
    private boolean isUrl(String path) {
        return path.startsWith("http://") || path.startsWith("https://");
    }

    /**
     * Проверка является ли входной путь шаблоном
     *
     * @param path путь
     *
     * @return {@code true}, если путь является шаблоном, иначе - {@code false}
     */
    private boolean containsWildcard(String path) {
        return path.contains(String.valueOf('*'));
    }

    /**
     * Метод парсинга и валидации URL
     *
     * @param path URL
     *
     * @return список с URl
     *
     * @throws ParameterException если не удалось подключиться к URL или успешно получить от нее ответ
     */
    private List<URI> parseUrl(String path) throws ParameterException {

        String errorMessage = "URL " + path + " не доступна";

        try {
            URI uri = new URI(path);

            HttpURLConnection connectionToUse = (HttpURLConnection) uri.toURL().openConnection();
            connectionToUse.setRequestMethod("HEAD");
            connectionToUse.setConnectTimeout(TIMEOUT);
            connectionToUse.setReadTimeout(TIMEOUT);

            int responseCode = connectionToUse.getResponseCode();
            connectionToUse.disconnect();

            if (SUCCESS_CODE <= responseCode && responseCode < ERROR_CODE) {
                return List.of(uri);
            }
        } catch (IOException | URISyntaxException e) {
            throw new ParameterException(errorMessage, e);
        }

        throw new ParameterException(errorMessage);
    }

    /**
     * Метод парсинга путей до файлов из шаблона
     *
     * @param pattern шаблон
     *
     * @return список всех файлов, подходящих под шаблон
     *
     * @throws IOException если не удалось получить доступ к стартовому файлу
     * @throws ParameterException если по шаблону не удалось найти ни одного валидного файла
     */
    private List<URI> parsePatternFiles(String pattern) throws IOException, ParameterException {
        List<URI> result = new ArrayList<>();

        String globPattern = pattern.replace("\\", String.valueOf('/'));
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);

        String baseDir = getBaseDir(globPattern);
        Path dir = Paths.get(baseDir);

        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile)
                .filter(Files::isReadable)
                .filter(p -> {
                    try {
                        return Files.size(p) > 0;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .filter(matcher::matches)
                .forEach(filePath -> result.add(filePath.toUri()));
        }

        if (result.isEmpty()) {
            throw new ParameterException("По шаблону " + pattern + " не найдено ни одного валидного файла");
        }

        return result;
    }

    /**
     * Получение базовой (стартовой) директории из шаблона
     *
     * @param globPattern шаблон
     *
     * @return базовая директория
     */
    private String getBaseDir(String globPattern) {
        int firstWildcard = Math.min(
            globPattern.contains(String.valueOf('*')) ? globPattern.indexOf('*') : globPattern.length(),
            globPattern.contains(String.valueOf('?')) ? globPattern.indexOf('?') : globPattern.length()
        );

        return firstWildcard > 0 && globPattern.lastIndexOf('/') > 0
            ? globPattern.substring(0, globPattern.lastIndexOf('/', firstWildcard))
            : String.valueOf('.');
    }

    /**
     * Метод парсинга пути для конкретного файла
     *
     * @param path путь до файла
     *
     * @return список с путем до файла
     *
     * @throws IOException если не удалось узнать размер файла
     * @throws ParameterException если файл не является файлом, его нельзя прочитать или он пустой
     */
    private List<URI> parseSingleFile(String path) throws IOException, ParameterException {
        Path pathToFile = Paths.get(path);

        if (Files.isRegularFile(pathToFile) && Files.isReadable(pathToFile)) {
            if (Files.size(pathToFile) > 0) {
                return List.of(pathToFile.toUri());
            } else {
                throw new ParameterException("Входной файл " + path + " пустой");
            }
        }

        throw new ParameterException("Некорректный путь или файл недоступен для чтения: " + path);
    }
}
