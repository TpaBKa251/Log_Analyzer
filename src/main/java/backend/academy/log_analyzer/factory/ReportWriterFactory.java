package backend.academy.log_analyzer.factory;

import backend.academy.log_analyzer.parameter.ArgsParameters;
import backend.academy.log_analyzer.writer.ReportWriter;
import backend.academy.log_analyzer.writer.impl.ReportWriterConsole;
import backend.academy.log_analyzer.writer.impl.ReportWriterFile;
import lombok.experimental.UtilityClass;

// Думаю тесты не нужны, логика простая
/**
 * Фабрика райтеров отчета
 */
@UtilityClass
public class ReportWriterFactory {

    /**
     * Метод, возвращающий конкретную реализацию райтера отчета
     *
     * @param parameters сконвертированные параметры
     *
     * @return экземпляр {@link ReportWriterConsole}, {@link ReportWriterFile}
     */
    public static ReportWriter createReportWriter(ArgsParameters parameters) {
        return parameters.inFile()
            ? new ReportWriterFile(parameters.outputFile())
            : new ReportWriterConsole();
    }
}
