package backend.academy.log_analyzer.mapper;

import backend.academy.log_analyzer.enums.HttpCodes;
import backend.academy.log_analyzer.log.LogReport;
import backend.academy.log_analyzer.maper.impl.ReportMapperToText;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.mockito.Mockito.when;

@DisplayName("Тесты класс LogMapperToText")
@ExtendWith(MockitoExtension.class)
public class ReportMapperToTextTest {

    @Mock
    ArgsParameters args;

    @Mock
    LogReport logReport;

    @InjectMocks
    ReportMapperToText reportMapper;

    @DisplayName("Тест корректности форматирования в текстовый формат")
    @Test
    void testMapToTextFormat() {

        String expectedMappedReport = """
            Основная статистика

            Общая информация:
                Файл(-ы):
                    /C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3-java\
            -TpaBKa251/src/main/resources/logs/logs.txt
                    /C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3-java\
            -TpaBKa251/src/main/resources/logs/some-logs/05-2023.txt
                    /C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3-java\
            -TpaBKa251/src/main/resources/logs/some-logs/2024/05
                    /C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3-java\
            -TpaBKa251/src/main/resources/logs/some-logs/2024/05.txt
                    /C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3-java\
            -TpaBKa251/src/main/resources/logs/some-logs/2024_08/logs
                    /C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3-java\
            -TpaBKa251/src/main/resources/logs/some-logs/logs-from-url.txt
                    /C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3-java\
            -TpaBKa251/src/main/resources/logs/some-logs/logs_2024.txt
                Начальная дата: 01.01.2015
                Конечная дата: 01.01.2016 20:12:34
                Поле для фильтрации: method
                Значение поля: GET
                Количество запросов: 205729
                Средний размер ответа: 659891b
                95p размера ответа: 1768b

            Часто запрашиваемые ресурсы (ТОП-3):
                /downloads/product_1: 121211
                /downloads/product_2: 84226
                /downloads/product_3: 292

            Часто встречающиеся коды ответа (ТОП-3):
                404 (Not Found): 135634
                304 (Not Modified): 53393
                200 (OK): 15942

            Дополнительная статистика

            Проценты кодов (200, 400 и 500):
                200: 8.0%
                400: 65.0%
                500: 0.0%

            Количество уникальных пользователей: 2656""";


        when(logReport.resources()).thenReturn(Set.of(
            "/C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3"
                + "-java-TpaBKa251/src/main/resources/logs/logs.txt",
            "/C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3"
                + "-java-TpaBKa251/src/main/resources/logs/some-logs/05-2023.txt",
            "/C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3"
                + "-java-TpaBKa251/src/main/resources/logs/some-logs/2024/05.txt",
            "/C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3"
                + "-java-TpaBKa251/src/main/resources/logs/some-logs/logs-from-url.txt",
            "/C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3"
                + "-java-TpaBKa251/src/main/resources/logs/some-logs/2024/05",
            "/C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3"
                + "-java-TpaBKa251/src/main/resources/logs/some-logs/2024_08/logs",
            "/C:/Users/ilial/IdeaProjects/backend_academy_2024_project_3"
                + "-java-TpaBKa251/src/main/resources/logs/some-logs/logs_2024.txt"
        ));
        when(logReport.getTotalCountRequests()).thenReturn(205729L);
        when(logReport.getAverageBytesSize()).thenReturn(659891L);
        when(logReport.get95thPercentile()).thenReturn(1768L);
        when(logReport.getPopularResources()).thenReturn(List.of(
            Map.entry("/downloads/product_1", 121211L),
            Map.entry("/downloads/product_2", 84226L),
            Map.entry("/downloads/product_3", 292L)
        ));
        when(logReport.getPopularCodeResponses()).thenReturn(List.of(
            Map.entry(HttpCodes.of(404), 135634L),
            Map.entry(HttpCodes.of(304), 53393L),
            Map.entry(HttpCodes.of(200), 15942L)
        ));
        when(logReport.getPercentOfCodeResponsesByType()).thenReturn(Map.of(
            400, 65.0,
            500, 0.0,
            200, 8.0
        ));
        when(logReport.getUniqueUsersCount()).thenReturn(2656L);

        when(args.from()).thenReturn(LocalDateTime.of(2015, 1, 1, 0, 0, 0));
        when(args.to()).thenReturn(LocalDateTime.of(2016, 1, 1, 20, 12, 34));
        when(args.filterField()).thenReturn("method");
        when(args.filterValue()).thenReturn("GET");

        String actualStringReport = reportMapper.mapLogToOutputFormat(logReport, args);

        List<String> expectedLines = Arrays.stream(expectedMappedReport.trim().split("\\r?\\n"))
            .map(String::trim)
            .toList();
        List<String> actualLines = Arrays.stream(actualStringReport.trim().split("\\r?\\n"))
            .map(String::trim)
            .toList();

        assertLinesMatch(expectedLines, actualLines);
    }
}
