package backend.academy.log_analyzer.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Перечисление возможных форматов вывода
 */
@RequiredArgsConstructor
@Getter
public enum OutputFormats {
    MARKDOWN("markdown", ".md"),
    ADOC("adoc", ".adoc"),
    TEXT("text", ".txt");

    /**
     * Название формата
     */
    private final String formatName;

    /**
     * Расширение формата
     */
    private final String extension;

    /**
     * Маппа, где ключ - название формата, а значение - соответствующий элемент перечисления
     */
    private static final Map<String, OutputFormats> FORMATS_MAP = new HashMap<>();

    static {
        for (OutputFormats format : values()) {
            FORMATS_MAP.put(format.extension, format);
        }
    }

    /**
     * Метод для получения элемента перечисления по расширению
     *
     * @param extension расширение формата
     *
     * @return элемент перечисления, соответствующий расширению формата
     */
    public static OutputFormats of(String extension) {
        return FORMATS_MAP.get(extension);
    }
}
