package backend.academy.log_analyzer;

import backend.academy.log_analyzer.factory.ReportWriterFactory;
import backend.academy.log_analyzer.log.LogAnalyzer;
import backend.academy.log_analyzer.module.ArgsParametersParserModule;
import backend.academy.log_analyzer.module.LogAnalyzerModule;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import backend.academy.log_analyzer.parser.ArgsParametersParser;
import com.beust.jcommander.ParameterException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * Класс для запуска приложения
 */
@Slf4j
public class LogAnalyzerApp {

    /**
     * Метод для старта приложения
     *
     * @param args аргументы командной строки
     */
    public void startApp(String[] args) {
        log.info("Программа начала работу");

        Injector injectorArgsParams = Guice.createInjector(new ArgsParametersParserModule());
        ArgsParametersParser parser = injectorArgsParams.getInstance(ArgsParametersParser.class);

        try {
            ArgsParameters parameters = parser.parse(args);
            analyzeAndWriteLog(parameters);
        } catch (ParameterException e) {
            log.error("Произошла ошибка: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Во время выполнения что-то пошло не так", e);
        }
    }

    /**
     * Метод для запуска анализа логов и вывода отчета
     *
     * @param parameters сконвертированные параметры
     *
     * @throws IOException если в процессе записи произошла ошибка
     */
    private void analyzeAndWriteLog(ArgsParameters parameters) throws IOException {
        Injector injector = Guice.createInjector(new LogAnalyzerModule(parameters.format(), parameters.path()));
        LogAnalyzer logAnalyzer = injector.getInstance(LogAnalyzer.class);

        String logReport = logAnalyzer.analyzeLogs(parameters);

        if (logReport.isEmpty()) {
            return;
        }

        ReportWriterFactory.createReportWriter(parameters).writeReport(logReport);

        if (parameters.inFile()) {
            log.info("Программа завершила работу, вы можете увидеть записанные результаты в {}",
                parameters.outputFile().getAbsolutePath());
        }
    }
}
