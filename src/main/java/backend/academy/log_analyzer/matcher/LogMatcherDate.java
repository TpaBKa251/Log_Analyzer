package backend.academy.log_analyzer.matcher;

import backend.academy.log_analyzer.parameter.ArgsParameters;
import java.time.LocalDateTime;

/**
 * Класс для проверки, что лог подходит под фильтры с датами
 */
public class LogMatcherDate {

    /**
     * Метод, проверяющий подходит ли лог по дате
     *
     * @param logTime время в логе
     * @param parameters сконвертированные параметры
     *
     * @return {@code  true}, если лог подходит, иначе {@code false}
     */
    public boolean isLogMatch(LocalDateTime logTime, ArgsParameters parameters) {
        LocalDateTime from = parameters.from();
        LocalDateTime to = parameters.to();

        if (from == null && to == null) {
            return true;
        }

        if (from == null) {
            return isLogTimeBeforeTo(logTime, to);
        }

        if (to == null) {
            return isLogTimeAfterFrom(logTime, from);
        }

        return isLogTimeBetweenFromAndTo(logTime, from, to);
    }

    /**
     * Метод, проверяющий, что время лога до или равно времени, до которого нужно собирать логи
     *
     * @param logTime время лога
     * @param to конечное время
     *
     * @return {@code  true}, если лог подходит, иначе {@code false}
     */
    private boolean isLogTimeBeforeTo(LocalDateTime logTime, LocalDateTime to) {
        return logTime.isBefore(to) || logTime.isEqual(to);
    }

    /**
     * Метод, проверяющий, что время лога после или равно времени, с которого нужно собирать логи
     *
     * @param logTime время лога
     * @param from начальное время
     *
     * @return {@code  true}, если лог подходит, иначе {@code false}
     */
    private boolean isLogTimeAfterFrom(LocalDateTime logTime, LocalDateTime from) {
        return logTime.isAfter(from) || logTime.isEqual(from);
    }

    /**
     * Метод, проверяющий, что лог находится между начальным и конечным временем
     *
     * @param logTime время лога
     * @param from начальное время
     * @param to конечное время
     *
     * @return {@code  true}, если лог подходит, иначе {@code false}
     */
    private boolean isLogTimeBetweenFromAndTo(LocalDateTime logTime, LocalDateTime from, LocalDateTime to) {
        if (!isLogTimeBeforeTo(logTime, to)) {
            return false;
        }

        return isLogTimeAfterFrom(logTime, from);
    }
}
