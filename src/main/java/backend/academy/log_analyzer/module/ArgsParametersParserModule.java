package backend.academy.log_analyzer.module;

import backend.academy.log_analyzer.parser.ArgsParser;
import backend.academy.log_analyzer.parser.PathParser;
import backend.academy.log_analyzer.parser.additional.Converters;
import backend.academy.log_analyzer.parser.additional.Validators;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Класс для настройки инъекции зависимостей
 * в класс {@link backend.academy.log_analyzer.parser.ArgsParametersParser} через библиотеку {@code Guice}
 */
public class ArgsParametersParserModule extends AbstractModule {

    @Provides
    @Singleton
    ArgsParser provideArgsParser() {
        return new ArgsParser();
    }

    @Provides
    @Singleton
    PathParser providePathParser() {
        return new PathParser();
    }

    @Provides
    @Singleton
    Validators provideValidators() {
        return new Validators();
    }

    @Provides
    @Singleton
    Converters provideConverters() {
        return new Converters();
    }
}


