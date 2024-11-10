package backend.academy.log_analyzer.matcher;

import backend.academy.log_analyzer.log.Log;
import backend.academy.log_analyzer.parameter.ArgsParameters;
import backend.academy.log_analyzer.util.LogFieldsStorage;
import com.beust.jcommander.ParameterException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Класс для проверки, что лог под фильтр со значением поля
 */
public class LogMatcherFilter {

    private static final int LAST_INDEX_FOR_DATE = 10;

    /**
     * Метод, проверяющий подходит ли лог по значению поля
     *
     * @param log лог
     * @param parameters сконвертированные параметры
     *
     * @return {@code  true}, если лог подходит, иначе {@code false}
     */
    public boolean isLogMatchByFilter(Log log, ArgsParameters parameters) {
        String filterField = parameters.filterField();
        String filterValue = parameters.filterValue();

        if (filterField.isEmpty()) {
            return true;
        }

        Object fieldValue = getFieldValue(log, filterField);

        if (fieldValue == null) {
            return false;
        }

        String fieldValueStr = (fieldValue instanceof Log.Request request)
            ? request.getRequestLine()
            : fieldValue.toString();

        return checkFieldMatch(fieldValueStr, filterField, filterValue);
    }

    /**
     * Метод для получения значения поля в логе
     *
     * @param log лог
     * @param fieldName имя поля
     *
     * @return значение поля
     */
    private Object getFieldValue(Log log, String fieldName) {
        try {
            Method getter = LogFieldsStorage.LOG_FIELDS.contains(fieldName)
                ? log.getClass().getMethod(fieldName)
                : log.request().getClass().getMethod(fieldName);

            return getter.invoke(
                LogFieldsStorage.LOG_FIELDS.contains(fieldName) ? log : log.request()
            );
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Метод, проверяющий, равно ли значение поля лога значению фильтра
     *
     * @param fieldValue значение поля в логе
     * @param fieldName имя поля
     * @param filterValue значение фильтра
     *
     * @return {@code  true}, если значения равны, иначе {@code false}
     */
    private boolean checkFieldMatch(String fieldValue, String fieldName, String filterValue) {
        boolean hasWildcard = filterValue.contains("*");

        if ("time".equals(fieldName)) {
            return hasWildcard
                ? matchesPattern(fieldValue, filterValue)
                : isLogDateMatch(fieldValue, filterValue);
        } else {
            return hasWildcard
                ? matchesPattern(fieldValue, filterValue)
                : fieldValue.equals(filterValue);
        }
    }

    /**
     * Метод, проверяющий, что лог подходит по времени
     *
     * @param logTime время лога
     * @param filterTime время в фильтре
     *
     * @return {@code  true}, если лог подходит, иначе {@code false}
     */
    private boolean isLogDateMatch(String logTime, String filterTime) {
        try {
            return filterTime.contains("T")
                ? logTime.equals(filterTime)
                : logTime.equals(filterTime.substring(0, LAST_INDEX_FOR_DATE));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ParameterException("Значение фильтра неверное."
                + " Необходимо вводить дату целиком, либо использовать шаблон", e);
        }
    }

    /**
     * Метод для сравнения значения поля в логе с шаблоном
     *
     * @param value значение поля
     * @param pattern шаблон
     *
     * @return {@code  true}, если лог подходит, иначе {@code false}
     */
    private boolean matchesPattern(String value, String pattern) {
        String regex = pattern.replace("*", ".*");
        return value.matches(regex);
    }
}

