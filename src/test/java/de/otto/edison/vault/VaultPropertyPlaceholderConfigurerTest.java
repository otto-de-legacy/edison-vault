package de.otto.edison.vault;

import org.springframework.mock.env.MockEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class VaultPropertyPlaceholderConfigurerTest {

    private VaultClient vaultClient;

    @Test
    public void shouldDoNothingIfVaultIsDeactivated() {

        // given
        final VaultPropertyPlaceholderConfigurer configurer = createTestConfigurer();
        when(vaultClient.readFields("testpath")).thenReturn(Collections.singletonMap("value", "secret"));

        final MockEnvironment environment = new MockEnvironment();
        environment.setProperty("edison.vault.enabled", "false");
        configurer.setEnvironment(environment);

        // when
        final String result = configurer.convertVaultProperty("vault://testpath#value");

        // then
        verifyZeroInteractions(vaultClient);
        assertThat(result, is("vault://testpath#value"));
    }

    @Test
    public void shouldDoNothingIfNoVaultSecret() throws Exception {

        // given
        final VaultPropertyPlaceholderConfigurer configurer = createTestConfigurer();
        when(vaultClient.readFields("")).thenReturn(Collections.singletonMap("value", "secret"));

        // when
        final String result = configurer.convertVaultProperty("value");

        // then
        verifyZeroInteractions(vaultClient);
        assertThat(result, is("value"));
    }

    @Test
    public void shouldLookupNonRootVaultSecret() throws Exception {

        // given
        final VaultPropertyPlaceholderConfigurer configurer = createTestConfigurer();
        when(vaultClient.readFields("testpath")).thenReturn(Collections.singletonMap("value", "secret"));

        // when
        final String result = configurer.convertVaultProperty("vault://testpath#value");

        // then
        assertThat(result, is("secret"));
    }

    @Test
    public void shouldLookupRootVaultSecret() throws Exception {

        // given
        final VaultPropertyPlaceholderConfigurer configurer = createTestConfigurer();
        when(vaultClient.readFields("")).thenReturn(Collections.singletonMap("value", "secret"));

        // when
        final String result = configurer.convertVaultProperty("vault://value");

        // then
        assertThat(result, is("secret"));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        vaultClient = mock(VaultClient.class);
    }

    private VaultPropertyPlaceholderConfigurer createTestConfigurer() {
        final VaultPropertyPlaceholderConfigurer configurer = new VaultPropertyPlaceholderConfigurer() {
            @Override
            protected VaultClient createVaultClient(final ConfigProperties configProperties) {
                return vaultClient;
            }
        };
        final MockEnvironment environment = new MockEnvironment();
        environment.setProperty("edison.vault.enabled", "true");
        configurer.setEnvironment(environment);
        return configurer;
    }
}