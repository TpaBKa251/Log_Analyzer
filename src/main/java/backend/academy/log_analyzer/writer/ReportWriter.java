package backend.academy.log_analyzer.writer;

import java.io.IOException;

/**
 * Интерфейс для вывода отчета (записи в консоль или файл)
 */
public interface ReportWriter {

    /**
     * Метод для записи отчета в консоль или файл
     *
     * @param logReport отчет
     *
     * @throws IOException если произошла ошибка записи
     */
    void writeReport(String logReport) throws IOException;
}
