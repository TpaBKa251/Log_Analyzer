package backend.academy.log_analyzer.util;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты хранилища с полями лога LogFieldsStorage")
public class LogFieldStorageTest {

    @DisplayName("Тест содержания всех имен полей в списке")
    @Test
    void shouldContainAllLogFields() {
        List<String> actualLogFields = LogFieldsStorage.ALL_FIELDS;
        List<String> expectedLogFields = List.of("ip", "user", "time", "request", "status", "bytes",
            "referer", "agent", "method", "endpoint", "version");

        assertThat(actualLogFields).containsExactlyInAnyOrderElementsOf(expectedLogFields);
    }
}
