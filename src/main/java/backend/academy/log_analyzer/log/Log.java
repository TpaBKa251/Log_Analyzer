package backend.academy.log_analyzer.log;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Рекорд лога формата NGINX
 *
 * @param ip remote_addr (IP пользователя)
 * @param user remote_user (имя пользователя)
 * @param time time_local (время создания лога)
 * @param request запрос
 * @param status код ответа
 * @param bytes body_bytes_sent (размер ответа)
 * @param referer http_referer (URL веб-страницы, откуда отправлен запрос)
 * @param agent http_user_agent (с какого приложения отправлен запрос)
 */
public record Log(
        String ip,
        String user,
        LocalDateTime time,
        Request request,
        int status,
        int bytes,
        String referer,
        String agent
) {

    private static final int IP = 1;
    private static final int USER = 2;
    private static final int TIME = 4;
    private static final int REQUEST = 5;
    private static final int STATUS = 6;
    private static final int BYTES = 7;
    private static final int REFERER = 8;
    private static final int AGENT = 9;

    private static final String NON_SPACE_GROUP = "(\\S+)\\s+";
    private static final String QUOTED_GROUP = "\"(.*?)\"\\s+";

    private static final int SIZE_OF_PARAMS_REQUEST = 3;

    /**
     * Метод для парсинга лога из строки
     *
     * @param logLine строка для парсинга
     *
     * @return объект лога или {@code null}, если строка не соответствует формату
     */
    @Nullable
    public static Log parse(String logLine) {
        Pattern logPattern = Pattern.compile(
            NON_SPACE_GROUP
                + NON_SPACE_GROUP
                + NON_SPACE_GROUP
                + "\\[(.*?)]\\s+"
                + QUOTED_GROUP
                + "(\\d{3})\\s+"
                + "(\\d+)\\s+"
                + QUOTED_GROUP
                + "\"(.*?)\""
        );

        Matcher matcher = logPattern.matcher(logLine);

        if (!matcher.matches()) {
            return null;
        }

        String remoteAddr = matcher.group(IP);
        String remoteUser = matcher.group(USER);
        LocalDateTime timeLocal = parseTimeLocal(matcher.group(TIME));
        Request request = Request.parse(matcher.group(REQUEST));
        int status = Integer.parseInt(matcher.group(STATUS));
        int bodyBytesSent = Integer.parseInt(matcher.group(BYTES));
        String httpReferer = matcher.group(REFERER);
        String httpUserAgent = matcher.group(AGENT);

        return new Log(
                remoteAddr,
                remoteUser, timeLocal,
                request,
                status,
                bodyBytesSent,
                httpReferer,
                httpUserAgent
        );
    }

    /**
     * Метод парсинга даты и времени из строки
     *
     * @param timeLocal строка с временем и датой
     *
     * @return дату и время
     */
    private static LocalDateTime parseTimeLocal(String timeLocal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeLocal, formatter);

        return zonedDateTime.toLocalDateTime();
    }

    /**
     * Вложенный рекорд запроса в логе
     *
     * @param method метод запроса
     * @param endpoint ресурс запроса (эндпоинт)
     * @param version HTTP версия запроса
     */
    public record Request(
            String method,
            String endpoint,
            String version
    ) {

        /**
         * Метод парсинга запроса из строки
         *
         * @param requestLine строка для парсинга
         *
         * @return объект запроса или {@code null}, если строка не соответствует шаблону
         */
        @Nullable
        private static Request parse(String requestLine) {
            String[] requestParts = requestLine.split(" ");

            if (requestParts.length != SIZE_OF_PARAMS_REQUEST) {
                return null;
            }

            return new Request(
                    requestParts[0],
                    requestParts[1],
                    requestParts[2]
            );
        }

        /**
         * Метод для сбора всего запроса в строку
         *
         * @return весь запрос в виде строки
         */
        public String getRequestLine() {
            return method + " " + endpoint + " " + version;
        }
    }
}
