package de.otto.edison.vault.example;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "test.config")
public class ExampleConfigurationProperties {

    private String one;
    private String two;
    private String three;

    public String getOne() {
        return one;
    }

    public void setOne(final String one) {
        this.one = one;
    }

    public String getTwo() {
        return two;
    }

    public void setTwo(final String two) {
        this.two = two;
    }

    public String getThree() {
        return three;
    }

    public void setThree(final String three) {
        this.three = three;
    }
}
