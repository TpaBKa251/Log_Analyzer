package backend.academy.log_analyzer.maper.impl;

import backend.academy.log_analyzer.enums.HttpCodes;
import backend.academy.log_analyzer.log.LogReport;
import backend.academy.log_analyzer.maper.ReportMapper;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Класс для форматирования в обычный текстовый вывод
 */
public class ReportMapperToText extends ReportMapper {

    private static final String EMPTY_INFO = "\t-";
    private static final String STATISTIC = "\t%s: %d%n";

    @Override
    public String mapLogToOutputFormat(LogReport report, ArgsParameters params) {
        return """
            Основная статистика%n%n\
            Общая информация:%n\
                Файл(-ы):%n%s%n\
                Начальная дата: %s%n\
                Конечная дата: %s%n\
                Поле для фильтрации: %s%n\
                Значение поля: %s%n\
                Количество запросов: %d%n\
                Средний размер ответа: %db%n\
                95p размера ответа: %db%n%n\
            Часто запрашиваемые ресурсы (ТОП-3):%n\
            %s%n\
            Часто встречающиеся коды ответа (ТОП-3):%n\
                %s%n%n\
            Дополнительная статистика%n%n\
            Проценты кодов (200, 400 и 500):%n\
                %s%n%n\
            Количество уникальных пользователей: %d""".formatted(
            mapFilesToText(report.resources()),
            mapDateToString(params.from()),
            mapDateToString(params.to()),
            params.filterField().isEmpty() ? "-" : params.filterField(),
            params.filterValue().isEmpty() ? "-" : params.filterValue(),
            report.getTotalCountRequests(),
            report.getAverageBytesSize(),
            report.get95thPercentile(),
            mapResourcesToText(report.getPopularResources()),
            mapCodesToText(report.getPopularCodeResponses()),
            mapCodesByType(report.getPercentOfCodeResponsesByType()),
            report.getUniqueUsersCount()
        );
    }

    /**
     * Метод для преобразования (форматирования) прочитанных ресурсов в формат для вывода
     *
     * @param paths пути до ресурсов
     * @return отформатированные пути
     */
    private static String mapFilesToText(Set<String> paths) {
        return paths.stream()
            .sorted()
            .map(path -> "\t\t" + path.replace("file://", ""))
            .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Метод для преобразования (форматирования) счетчика эндпоинтов в формат для вывода
     *
     * @param resources список эндпоинтов
     * @return отформатированные эндпоинты
     */
    private String mapResourcesToText(List<Map.Entry<String, Long>> resources) {
        if (resources.isEmpty()) {
            return EMPTY_INFO;
        }

        List<String> lines = new ArrayList<>(resources.size());

        int buffer = 0;

        for (Map.Entry<String, Long> entry : resources) {
            String line = String.format(STATISTIC, entry.getKey(), entry.getValue());

            buffer += line.length();

            lines.add(line);
        }

        StringBuilder resourcesTable = new StringBuilder(buffer);

        for (String line : lines) {
            resourcesTable.append(line);
        }

        return resourcesTable.toString();
    }

    /**
     * Метод для преобразования (форматирования) счетчика кодов ответа в формат для вывода
     *
     * @param httpCodes список кодов ответа
     * @return отформатированные коды ответа
     */
    private String mapCodesToText(List<Map.Entry<HttpCodes, Long>> httpCodes) {
        if (httpCodes.isEmpty()) {
            return EMPTY_INFO;
        }

        List<String> lines = new ArrayList<>(httpCodes.size());

        int buffer = 0;

        for (Map.Entry<HttpCodes, Long> entry : httpCodes) {
            String line = String.format(
                "    %s (%s): %d%n",
                entry.getKey().code(),
                entry.getKey().description(),
                entry.getValue()
            );

            buffer += line.length();

            lines.add(line);
        }

        StringBuilder codesString = new StringBuilder(buffer);

        for (String line : lines) {
            codesString.append(line);
        }

        return codesString.toString().trim();
    }

    /**
     * Метод для преобразования (форматирования) счетчика 200-х, 400-х и 500-х кодов ответа
     *
     * @param codesByType маппа счетчика
     * @return отформатированные соотношения кодов ответа
     */
    private String mapCodesByType(Map<Integer, Double> codesByType) {

        List<String> lines = new ArrayList<>(codesByType.size());
        List<Integer> sortedKeys = new ArrayList<>(codesByType.keySet());
        Collections.sort(sortedKeys);

        int buffer = 0;

        for (Integer code : sortedKeys) {
            String line = String.format("    %d: %s%s%n", code, codesByType.get(code), '%');

            buffer += line.length();

            lines.add(line);
        }

        StringBuilder codesByTypeText = new StringBuilder(buffer);

        for (String line : lines) {
            codesByTypeText.append(line);
        }

        return codesByTypeText.toString().trim();
    }
}



