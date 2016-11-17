package de.otto.edison.vault;

import org.springframework.mock.env.MockEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class VaultPropertySourceTest {

    private VaultClient vaultClient;

    @Test
    public void shouldDoNothingIfVaultIsDeactivated() {

        // given
        final List<String> testProperties = Collections.singletonList("testpath.value");
        when(vaultClient.readFields("testpath")).thenReturn(Collections.singletonMap("value", "secret"));

        final VaultPropertySource source = createTestPropertySource(testProperties, false);

        // when
        final String result = (String) source.getProperty("testpath.value");

        // then
        verifyZeroInteractions(vaultClient);
        assertThat(source.getPropertyNames().length, is(0));
        assertThat(result, nullValue());
    }

    @Test
    public void shouldDoNothingIfNoVaultSecret() throws Exception {

        // given
        final List<String> testProperties = Collections.emptyList();
        when(vaultClient.readFields("")).thenReturn(Collections.singletonMap("value", "secret"));

        final VaultPropertySource source = createTestPropertySource(testProperties, true);

        // when
        final String result = (String) source.getProperty("testpath.value");

        // then
        verifyZeroInteractions(vaultClient);
        assertThat(source.getPropertyNames().length, is(0));
        assertThat(result, nullValue());
    }

    @Test
    public void shouldLookupNonRootVaultSecret() throws Exception {

        // given
        final List<String> testProperties = Collections.singletonList("testpath.value");
        when(vaultClient.readFields("testpath")).thenReturn(Collections.singletonMap("value", "secret"));

        final VaultPropertySource source = createTestPropertySource(testProperties, true);

        // when
        final String result = (String) source.getProperty("testpath.value");

        // then
        assertThat(source.getPropertyNames().length, is(1));
        assertThat(result, is("secret"));
    }

    @Test
    public void shouldLookupRootVaultSecret() throws Exception {

        // given
        final List<String> testProperties = Collections.singletonList("testpath-value");
        when(vaultClient.readFields("")).thenReturn(Collections.singletonMap("testpath-value", "secret"));

        final VaultPropertySource source = createTestPropertySource(testProperties, true);

        // when
        final String result = (String) source.getProperty("testpath-value");

        // then
        assertThat(result, is("secret"));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        vaultClient = mock(VaultClient.class);
    }

    private VaultPropertySource createTestPropertySource(final List<String> properties, final boolean enabled) {
        final MockEnvironment environment = new MockEnvironment();
        environment.setProperty("edison.vault.enabled", Boolean.toString(enabled));
        environment.setProperty("edison.vault.properties", properties.stream().collect(Collectors.joining(",")));
        final VaultPropertySource source = new VaultPropertySource("testSource", new ConfigProperties(environment)) {
            @Override
            protected VaultClient createVaultClient(final ConfigProperties configProperties) {
                return vaultClient;
            }
        };
        return source;
    }
}