package de.otto.edison.vault.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@ComponentScan("de.otto.edison.vault")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ExampleApplication.class)
public class ExampleApplicationTests {

    @Value("${keyOne.value}")
    private String secretOne;

    @Value("${keyTwo.value}")
    private String secretTwo;

    @Value("${keyThree.value}")
    private String secretThree;

    @Autowired
    ExampleBean exampleBean;

    @Test
    public void shouldHaveWiredValuesForValueProcessing() {
        assertThat(secretOne, is("secretNumberOne"));
        assertThat(secretTwo, is("secretNumberTwo"));
        assertThat(secretThree, is("secretNumberThree"));
    }

    @Test
    public void shouldHaveWiredValuesForConfigurationProcessing() {
        assertThat(exampleBean.getOne(), is("1"));
        assertThat(exampleBean.getTwo(), is("2"));
        assertThat(exampleBean.getThree(), is("3"));
    }
}
