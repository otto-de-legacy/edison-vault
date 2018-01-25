package de.otto.edison.vault.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

//@ComponentScan("de.otto.edison.vault")
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = ExampleApplication.class)
public class ExampleApplicationTests  {

    @Value("${keyOne}")
    private String secretOne;

    @Value("${keyTwo}")
    private String secretTwo;

    @Value("${keyThree}")
    private String secretThree;

	//@Test
	public void shouldHaveWiredValues() {
		assertThat(secretOne, is("secretNumberOne"));
		assertThat(secretTwo, is("secretNumberTwo"));
		assertThat(secretThree, is("secretNumberThree"));
	}
}
