package backend.academy.log_analyzer.writer.impl;

import backend.academy.log_analyzer.writer.ReportWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

// Не вижу смысла тестов
/**
 * Класс для записи отчета в консоль
 */
public class ReportWriterConsole implements ReportWriter {

    @Override
    public void writeReport(String logReport) {
        try (PrintWriter out =
                 new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true)) {
            out.println(logReport);
        }
    }
}
