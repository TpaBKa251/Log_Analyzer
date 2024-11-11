package backend.academy.log_analyzer.util;

import backend.academy.log_analyzer.log.Log;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

/**
 * Класс-хранилище с полями лога (параметрами NGINX лога)
 */
@UtilityClass
public class LogFieldsStorage {

    private static final int COUNT_OF_FIELDS = 8;

    public static final List<String> LOG_FIELDS = Arrays.stream(Log.class.getDeclaredFields())
        .limit(COUNT_OF_FIELDS)
        .map(Field::getName)
        .toList();

    public static final List<String> ALL_FIELDS = Stream.concat(
            LOG_FIELDS.stream(),
            Arrays.stream(Log.Request.class.getDeclaredFields()).map(Field::getName)
        )
        .toList();
}

