package backend.academy.log_analyzer.log;

import backend.academy.log_analyzer.factory.ReaderFactory;
import backend.academy.log_analyzer.maper.ReportMapper;
import backend.academy.log_analyzer.matcher.LogMatcherDate;
import backend.academy.log_analyzer.matcher.LogMatcherFilter;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import com.beust.jcommander.ParameterException;
import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Анализатор логов из заданных ридеров
 */
@Slf4j
public class LogAnalyzer {

    private static final String READER_CLOSING_ERROR = "Во время закрытия потока чтения произошла ошибка";

    private final LogMatcherDate logMatcherDate;
    private final LogMatcherFilter logMatcherFilter;
    private final ReportMapper reportMapper;
    private final LogReport report;
    private final Map<BufferedReader, URI> readers;

    @Inject
    public LogAnalyzer(
        LogMatcherDate logMatcherDate,
        LogMatcherFilter logMatcherFilter,
        ReportMapper reportMapper,
        LogReport report,
        Map<BufferedReader, URI> readers

    ) {
        this.logMatcherDate = logMatcherDate;
        this.logMatcherFilter = logMatcherFilter;
        this.reportMapper = reportMapper;
        this.report = report;
        this.readers = readers;
    }

    /**
     * Метод анализа логов. Проводит фильтрацию, собирает статистику и формирует текстовый отчет в нужном формате
     *
     * @param params сконвертированные параметры
     * @return текстовый отчет в нужном формате
     * @throws IOException если произошла ошибка создания ридеров
     * @see ReaderFactory#createReaders(List)
     */
    public String analyzeLogs(ArgsParameters params) {
        try {
            final String[] uris = new String[1];

            readers.keySet().stream()
                .flatMap(reader -> {
                    uris[0] = readers.get(reader).toString();
                    return reader.lines().onClose(() -> closeQuietly(reader));
                })
                .map(Log::parse)
                .filter(log -> log != null
                    && logMatcherDate.isLogMatch(log.time(), params)
                    && logMatcherFilter.isLogMatchByFilter(log, params))
                .forEach(filteredLog -> report.addAllStats(filteredLog, uris[0]));

            if (report.getTotalCountRequests() == 0) {
                throw new ParameterException("Во входных файлах/URL не найдено ни одного лога."
                    + "\nПросмотренные ресурсы: " +
                    readers.values().stream().map(u -> "\n\t" + u.toString()).toList() + "\nФильтры: "
                        + "\n\tначальная дата: " + params.from() + "\n\tконечная дата: " + params.to()
                        + "\n\tполе для фильтрации: " + params.filterField()
                        + "\n\tзначение: " + params.filterValue());
            }

            return reportMapper.mapLogToOutputFormat(report, params);
        } finally {
            readers.keySet().forEach(this::closeQuietly);
        }
    }

    /**
     * Метод "тихого" закрытия ридера (без try-catch)
     *
     * @param reader ридер для закрытия
     */
    private void closeQuietly(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            log.error(READER_CLOSING_ERROR, e);
        }
    }
}
