package backend.academy.log_analyzer.log;

import backend.academy.log_analyzer.enums.HttpCodes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.Getter;

/**
 * Класс со статистикой проанализированных логов. По нему формируется отчет
 */
public class LogReport {

    private static final int MAX_SIZE_FOR_STAT = 3;
    private static final double PERCENTILE_FACTOR = .95;
    private static final int SUCCESSFUL_RESPONSES = 200;
    private static final int REDIRECTION_RESPONSES = 300;
    private static final int CLIENT_ERROR = 400;
    private static final int SERVER_ERROR = 500;
    private static final int CODE_FACTOR = 100;

    /**
     * Запрашиваемые ресурсы и их количество
     */
    private final Map<String, Long> requestedResources = new HashMap<>();
    /**
     * Все коды ответов и их количество
     */
    private final Map<HttpCodes, Long> codeResponses = new EnumMap<>(HttpCodes.class);
    /**
     * Все размеры ответов
     */
    private final List<Long> bytesSizes = new ArrayList<>();

    /**
     * Количество ответов по типу кода (200, 400 и 500)
     */
    private final Map<Integer, Double> codeResponsesByType = new HashMap<>();
    /**
     * Уникальные пользователи
     */
    private final Set<String> uniqueUsers = new HashSet<>();

    /**
     * Ресурсы, из которых успешно прочитался хотя бы один лог
     */
    @Getter
    private final Set<String> resources = new HashSet<>();

    /**
     * Флаг, что процентные соотношения кодов ответа уже посчитаны
     */
    private boolean isPercentOfCodeCalculated = false;

    /**
     * Метод добавления всей статистики
     *
     * @param log лог
     * @param resourceUri ресурс, откуда получен лог
     */
    public void addAllStats(Log log, String resourceUri) {
        addByteSize(log.bytes());
        addRequestedResource(log.request().endpoint());
        addCodeResponses(HttpCodes.of(log.status()));
        addCodeResponsesByType(log.status());
        addUser(log.ip());
        addResource(resourceUri);
    }

    /**
     * Метод добавления размера ответа
     *
     * @param byteSize размер ответа
     */
    private void addByteSize(long byteSize) {
        bytesSizes.add(byteSize);
    }

    /**
     * Метод добавления/обновления счетчика эндпоинтов
     * @param resourceName эндпоинт (запрашиваемый ресурс)
     */
    private void addRequestedResource(String resourceName) {
        requestedResources.merge(resourceName, 1L, Long::sum);
    }

    /**
     * Метод добавления/обновления счетчика кодов ответа сервера
     *
     * @param httpCodes код ответа сервера
     */
    private void addCodeResponses(HttpCodes httpCodes) {
        codeResponses.merge(httpCodes, 1L, Long::sum);
    }

    /**
     * Метод добавления/обновления счетчика 200-х, 400-х и 500-х кодов ответа сервера
     *
     * @param httpCode код ответа сервера
     */
    private void addCodeResponsesByType(int httpCode) {
        int code = httpCode / CODE_FACTOR * CODE_FACTOR;

        if (code != REDIRECTION_RESPONSES) {
            codeResponsesByType.merge(code, 1.0, Double::sum);
        }
    }

    /**
     * Метод добавления пользователя, отправившего запрос
     *
     * @param remoteAddr пользователь
     */
    private void addUser(String remoteAddr) {
        uniqueUsers.add(remoteAddr);
    }

    /**
     * Метод добавления ресурса, откуда прочитали лог
     *
     * @param resourcePath путь до ресурса
     */
    private void addResource(String resourcePath) {
        resources.add(resourcePath);
    }

    public long getTotalCountRequests() {
        return bytesSizes.size();
    }

    /**
     * Получить топ-3 запрашиваемых ресурсов
     *
     * @return список пар ключ-значение для 3-х или менее ресурсов
     */
    public List<Entry<String, Long>> getPopularResources() {
        return requestedResources.entrySet().stream()
            .sorted(Entry.<String, Long>comparingByValue().reversed())
            .limit(MAX_SIZE_FOR_STAT)
            .toList();
    }

    /**
     * Получить топ-3 кодов ответа
     *
     * @return список пар ключ-значение для 3-х или менее кодов
     */
    public List<Entry<HttpCodes, Long>> getPopularCodeResponses() {
        return codeResponses.entrySet().stream()
            .sorted(Entry.<HttpCodes, Long>comparingByValue().reversed())
            .limit(MAX_SIZE_FOR_STAT)
            .toList();
    }

    /**
     * Получить средний размер ответов
     *
     * @return средний размер ответов
     */
    public long getAverageBytesSize() {
        long totalByteSize = 0;
        for (long byteSize : bytesSizes) {
            totalByteSize += byteSize;
        }

        long totalCountRequests = bytesSizes.size();

        return totalByteSize / totalCountRequests;
    }

    /**
     * Получить 95% перцентиль размера ответа
     *
     * @return 95% перцентиль размера ответа
     */
    public long get95thPercentile() {
        Collections.sort(bytesSizes);

        int index = (int) Math.ceil(PERCENTILE_FACTOR * bytesSizes.size()) - 1;

        return bytesSizes.get(index);
    }

    /**
     * Получить процент ответов по типу кода (процент 200-, 400- и 500-х)
     *
     * @return пары ключ-значение для типов кодов ответа
     */
    public Map<Integer, Double> getPercentOfCodeResponsesByType() {
        if (!isPercentOfCodeCalculated) {
            int totalCountRequests = bytesSizes.size();

            codeResponsesByType.put(SUCCESSFUL_RESPONSES,
                (codeResponsesByType.getOrDefault(SUCCESSFUL_RESPONSES, 0.0)
                / totalCountRequests * CODE_FACTOR));
            codeResponsesByType.put(CLIENT_ERROR,
                (codeResponsesByType.getOrDefault(CLIENT_ERROR, 0.0)
                / totalCountRequests * CODE_FACTOR));
            codeResponsesByType.put(SERVER_ERROR,
                (codeResponsesByType.getOrDefault(SERVER_ERROR, 0.0)
                / totalCountRequests * CODE_FACTOR));

            isPercentOfCodeCalculated = true;
        }

        return codeResponsesByType;
    }

    /**
     * Получить количество уникальных юзеров (по IP)
     *
     * @return количество уникальных юзеров
     */
    public long getUniqueUsersCount() {
        return uniqueUsers.size();
    }
}
