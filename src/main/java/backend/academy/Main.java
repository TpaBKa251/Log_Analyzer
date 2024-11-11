package backend.academy;

import backend.academy.log_analyzer.LogAnalyzerApp;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Main {
    public static void main(String[] args) {
        new LogAnalyzerApp().startApp(args);
    }
}
