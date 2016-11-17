package de.otto.edison.vault.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ExampleConfigurationProperties.class)
public class ExampleConfiguration {

    @Autowired
    private ExampleConfigurationProperties properties;

    @Bean
    public ExampleBean exampleBean() {
        return new ExampleBean(properties.getOne(), properties.getTwo(), properties.getThree());
    }
}
