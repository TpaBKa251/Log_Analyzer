package backend.academy.log_analyzer.factory;

import com.beust.jcommander.ParameterException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

// Я не знаю как избавиться от спотбагов, если это необходимо, подскажите, пожалуйста
/**
 * Фабрика для создания ридеров файлов и URL
 */
@SuppressFBWarnings(value = {"URLCONNECTION_SSRF_FD", "PATH_TRAVERSAL_IN"})
@UtilityClass
@Slf4j
public class ReaderFactory {

    /**
     * Метод для создания маппы ридеров файлов и URL. Ключ - URI, значение - ридер для этого URI
     *
     * @param path путь до файла, шаблон или URL
     *
     * @return маппу ридеров для URI
     *
     * @throws IOException если не удалось создать ридер для одного файла (если подан путь до конкретного файла),
     * URL или всех файлов (если подан шаблон)
     */
    public static Map<BufferedReader, URI> createReaders(List<URI> path) throws IOException {
        Map<BufferedReader, URI> readers = new HashMap<>();

        if (path.size() == 1) {
            addReaderFromSinglePath(path.getFirst(), readers);
        } else {
            addPatternReaders(path, readers);
        }

        if (readers.isEmpty()) {
            throw new IOException("Не удалось создать ни одного потока чтения файла или URL");
        }

        return readers;
    }

    /**
     * Метод для добавления ридера, если список URI состоит из одного пути
     *
     * @param pathToResource путь до ресурса
     * @param readers маппа ридеров для заполнения
     *
     * @throws IOException если произошла ошибка создания ридера для одного файла или URL
     */
    private static void addReaderFromSinglePath(URI pathToResource, Map<BufferedReader, URI> readers)
            throws IOException {
        switch (pathToResource.getScheme()) {
            case "file" -> {
                try {
                    addSingleFileReader(pathToResource, readers);
                } catch (FileNotFoundException e) {
                    throw new IOException("Файл не доступен", e);
                }
            }
            case "http", "https" -> {
                try {
                    addUrlReader(pathToResource, readers);
                } catch (IOException e) {
                    throw new IOException("URL не доступна", e);
                }
            }
            default -> throw new ParameterException("Неизвестный вид ресурса");
        }
    }

    // Не думаю, что нужно тестировать
    /**
     * Метод для добавления ридер для URL
     *
     * @param input URI до URL
     * @param readers маппа ридеров для заполнения
     *
     * @throws IOException если произошла ошибка создания ридера
     */
    private static void addUrlReader(URI input, Map<BufferedReader, URI> readers) throws IOException {
        URL url = input.toURL();
        readers.put(new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)), input);
    }

    //
    /**
     * Метод добавления ридеров для файлов по шаблону
     *
     * @param path список URI для каждого файла из шаблона
     * @param readers маппа ридеров для заполнения
     */
    private static void addPatternReaders(List<URI> path, Map<BufferedReader, URI> readers) {
        for (URI pathToResource : path) {
            Path resourcePath = Paths.get(pathToResource);

            // Если ридер не создался - игнорируем, у нас еще есть много файлов
            try {
                readers.put(new BufferedReader(new FileReader(resourcePath.toFile(),
                        StandardCharsets.UTF_8)), pathToResource);
            } catch (IOException ignored) {
            }
        }
    }

    // Не думаю, что нужно тестировать
    /**
     * Метод для создания ридера для конкретного файла
     *
     * @param input URI до файла
     * @param readers список ридеров для заполнения
     *
     * @throws IOException если произошла ошибка создания ридера
     */
    private static void addSingleFileReader(URI input, Map<BufferedReader, URI> readers) throws IOException {
        Path path = Paths.get(input);
        readers.put(new BufferedReader(new FileReader(path.toFile(), StandardCharsets.UTF_8)), input);
    }
}
