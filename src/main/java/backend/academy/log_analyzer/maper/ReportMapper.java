package backend.academy.log_analyzer.maper;

import backend.academy.log_analyzer.log.LogReport;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Маппер для преобразования статистики проанализированных логов в текстовый отчет в нужном формате
 */
public abstract class ReportMapper {

    protected static final String COL_SEPARATOR = " | ";
    protected static final String EMPTY_ROW_ASCII = "| - | - | - ";
    protected static final String NEW_LINE_ASCII = " \n";
    protected static final String EMPTY_ROW_MD = "| - | - | - |";
    protected static final String NEW_LINE_MD = " |\n";

    /**
     * Метод, возвращающий строку, содержащую отчет в нужном формате
     *
     * @param report статистика
     * @param params сконвертированные параметры
     * @return отчет в нужном формате в виде строки
     */
    public abstract String mapLogToOutputFormat(LogReport report, ArgsParameters params);

    /**
     * Метод для преобразования даты и времени в удобный формат для чтения
     *
     * @param time дата и время
     * @return строку даты и времени
     */
    protected String mapDateToString(LocalDateTime time) {
        if (time == null) {
            return String.valueOf('-');
        }

        if (time.getHour() == 0 && time.getMinute() == 0 && time.getSecond() == 0) {
            return time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        return time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    /**
     * Метод преобразования статистики процентного соотношения кодов ответа в формат для вывода
     *
     * @param codesByType маппа кодов ответа
     * @param isMd        является ли преобразование в markdown
     * @return строка процентного соотношения кодов ответа
     */
    protected String mapCodesByType(Map<Integer, Double> codesByType, boolean isMd) {
        if (codesByType.isEmpty()) {
            return isMd ? EMPTY_ROW_MD : EMPTY_ROW_ASCII;
        }

        List<String> lines = new ArrayList<>(codesByType.size());

        int[] buffer = new int[1];

        codesByType.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                    String line = "| " + entry.getKey() + COL_SEPARATOR
                        + entry.getValue() + '%' + (isMd ? NEW_LINE_MD : NEW_LINE_ASCII);

                    buffer[0] += line.length();

                    lines.add(line);
                }
            );

        StringBuilder codesString = new StringBuilder(buffer[0]);

        for (String line : lines) {
            codesString.append(line);
        }

        return codesString.toString();
    }
}
