package backend.academy.log_analyzer.module;

import backend.academy.log_analyzer.enums.OutputFormats;
import backend.academy.log_analyzer.factory.ReaderFactory;
import backend.academy.log_analyzer.factory.ReportMapperFactory;
import backend.academy.log_analyzer.log.LogReport;
import backend.academy.log_analyzer.maper.ReportMapper;
import backend.academy.log_analyzer.matcher.LogMatcherDate;
import backend.academy.log_analyzer.matcher.LogMatcherFilter;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * Класс для настройки инъекции зависимостей
 * в класс {@link backend.academy.log_analyzer.log.LogAnalyzer} через библиотеку {@code Guice}
 */
@RequiredArgsConstructor
public class LogAnalyzerModule extends AbstractModule {

    private final OutputFormats outputFormat;
    private final List<URI> paths;

    @Override
    protected void configure() {
        bind(LogMatcherDate.class).toInstance(new LogMatcherDate());
        bind(LogMatcherFilter.class).toInstance(new LogMatcherFilter());
        bind(LogReport.class).toInstance(new LogReport());
    }

    @Provides
    @Singleton
    ReportMapper provideReportMapper() {
        return ReportMapperFactory.getMapper(outputFormat);
    }

    @Provides
    Map<BufferedReader, URI> provideReaders() {
        try {
            return ReaderFactory.createReaders(paths);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при создании читателей для файлов или URL", e);
        }
    }
}
