package de.otto.edison.vault.example;

public class ExampleBean {

    private final String one;
    private final String two;
    private final String three;

    public ExampleBean(final String one, final String two, final String three) {
        this.one = one;
        this.two = two;
        this.three = three;
    }

    public String getOne() {
        return one;
    }

    public String getTwo() {
        return two;
    }

    public String getThree() {
        return three;
    }
}
