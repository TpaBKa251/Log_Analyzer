package backend.academy.log_analyzer.factory;

import backend.academy.log_analyzer.enums.OutputFormats;
import backend.academy.log_analyzer.maper.ReportMapper;
import backend.academy.log_analyzer.maper.impl.ReportMapperToAsciiDoc;
import backend.academy.log_analyzer.maper.impl.ReportMapperToMarkdown;
import backend.academy.log_analyzer.maper.impl.ReportMapperToText;
import lombok.experimental.UtilityClass;

// Думаю тесты не нужны, логика простая
/**
 * Фабрика мапперов отчета
 */
@UtilityClass
public class ReportMapperFactory {

    /**
     * Метод возвращающий конкретную реализацию маппера
     *
     * @param outputFormat формат вывода
     *
     * @return экземпляр {@link ReportMapperToText}, {@link ReportMapperToMarkdown}, {@link ReportMapperToAsciiDoc}
     */
    public static ReportMapper getMapper(OutputFormats outputFormat) {
        return switch (outputFormat) {
            case ADOC -> new ReportMapperToAsciiDoc();
            case MARKDOWN -> new ReportMapperToMarkdown();
            case TEXT -> new ReportMapperToText();
        };
    }
}
