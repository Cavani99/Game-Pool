package project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Locale;

@Configuration
public class LocaleConfig {

    @Bean
    public List<Locale> supportedLocales() {
        return List.of(Locale.ENGLISH, new Locale("bg"));
    }
}
