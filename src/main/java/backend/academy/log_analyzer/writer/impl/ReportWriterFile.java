package backend.academy.log_analyzer.writer.impl;

import backend.academy.log_analyzer.writer.ReportWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;

// Не вижу смысла тестов
/**
 * Класс для записи отчета в файл
 */
@RequiredArgsConstructor
public class ReportWriterFile implements ReportWriter {

    private final File file;

    @Override
    public void writeReport(String logReport) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write(logReport);
        }
    }
}
