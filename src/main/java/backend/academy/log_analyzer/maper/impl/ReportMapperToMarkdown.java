package backend.academy.log_analyzer.maper.impl;

import backend.academy.log_analyzer.enums.HttpCodes;
import backend.academy.log_analyzer.log.LogReport;
import backend.academy.log_analyzer.maper.ReportMapper;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Класс для форматирования отчета в Markdown
 */
public class ReportMapperToMarkdown extends ReportMapper {

    @Override
    public String mapLogToOutputFormat(LogReport report, ArgsParameters params) {
        return """
            ## Основная статистика%n\
            ### Общая информация%n\
            |        Метрика        |     Значение |%n\
            |:---------------------:|-------------:|%n\
            |       Файл(-ы)        | %s |%n\
            |    Начальная дата     |   %s |%n\
            |     Конечная дата     |            %s |%n\
            |  Поле для фильтрации  |           %s |%n\
            |   Значение поля       |           %s |%n\
            |  Количество запросов  |       %d |%n\
            | Средний размер ответа |         %db |%n\
            |   95p размера ответа  |         %db |%n\
            ### Часто запрашиваемые ресурсы (ТОП-3)%n\
            |     Ресурс      | Количество |%n\
            |:---------------:|-----------:|%n\
            %s%n\
            ### Часто встречающиеся коды ответа (ТОП-3)%n\
            | Код |          Имя          | Количество |%n\
            |:---:|:---------------------:|-----------:|%n\
            %s%n\
            ## Дополнительная статистика%n\
            ### Проценты кодов (200, 400 и 500)%n\
            | Тип кода | Процентное соотношение |%n\
            |:--------:|-----------------------:|%n\
            %s%n\
            ### Количество уникальных пользователей: %d""".formatted(
            mapFilesToMarkdown(report.resources()),
            mapDateToString(params.from()),
            mapDateToString(params.to()),
            params.filterField().isEmpty() ? '-' : params.filterField(),
            params.filterValue().isEmpty() ? '-' : params.filterValue(),
            report.getTotalCountRequests(),
            report.getAverageBytesSize(),
            report.get95thPercentile(),
            mapResourcesToMarkdown(report.getPopularResources()),
            mapCodesToMarkdown(report.getPopularCodeResponses()),
            mapCodesByType(report.getPercentOfCodeResponsesByType(), true),
            report.getUniqueUsersCount()
        );
    }

    /**
     * Метод для преобразования (форматирования) прочитанных ресурсов в формат для вывода
     *
     * @param paths пути до ресурсов
     * @return отформатированные пути
     */
    private static String mapFilesToMarkdown(Set<String> paths) {
        List<String> lines = new ArrayList<>(paths.size());

        int[] buffer = new int[1];

        paths.stream().sorted().forEach(path -> {
            String escapedPath = StringEscapeUtils.escapeHtml4(path.replace("file://", ""));
            String line = '`' + escapedPath + '`' + "<br>";
            buffer[0] += line.length();

            lines.add(line);
        });

        StringBuilder pathsString = new StringBuilder(buffer[0]);

        for (String line : lines) {
            pathsString.append(line);
        }

        return pathsString.toString();
    }

    /**
     * Метод для преобразования (форматирования) счетчика эндпоинтов в формат для вывода
     *
     * @param resources список эндпоинтов
     * @return отформатированные эндпоинты
     */
    private String mapResourcesToMarkdown(List<Map.Entry<String, Long>> resources) {
        if (resources.isEmpty()) {
            return "| - | - |%n";
        }

        List<String> lines = new ArrayList<>(resources.size());

        int buffer = 0;

        for (Map.Entry<String, Long> entry : resources) {
            String escapedKey = StringEscapeUtils.escapeHtml4(entry.getKey());
            String line = "| `" + escapedKey + "` | " + entry.getValue() + NEW_LINE_MD;

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
    private String mapCodesToMarkdown(List<Map.Entry<HttpCodes, Long>> httpCodes) {
        if (httpCodes.isEmpty()) {
            return EMPTY_ROW_MD;
        }

        List<String> lines = new ArrayList<>(httpCodes.size());

        int buffer = 0;

        for (Map.Entry<HttpCodes, Long> entry : httpCodes) {
            HttpCodes httpCode = entry.getKey();

            String escapedDescription = StringEscapeUtils.escapeHtml4(httpCode.description());
            String line = "| " + httpCode.code() + COL_SEPARATOR
                + escapedDescription + COL_SEPARATOR
                + entry.getValue() + NEW_LINE_MD;

            buffer += line.length();

            lines.add(line);
        }

        StringBuilder codesString = new StringBuilder(buffer);

        for (String line : lines) {
            codesString.append(line);
        }

        return codesString.toString().trim();
    }
}
