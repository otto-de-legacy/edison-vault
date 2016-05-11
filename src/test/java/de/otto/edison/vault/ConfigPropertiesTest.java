package de.otto.edison.vault;

import org.hamcrest.Matchers;
import org.springframework.core.env.Environment;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigPropertiesTest {

    @Test
    public void shouldHaveConfigValues() throws Exception {
        // Given
        Environment environment = mock(Environment.class);
        when(environment.getProperty("edison.vault.enabled")).thenReturn("true");
        when(environment.getProperty("edison.vault.base-url")).thenReturn("someBaseUrl");
        when(environment.getProperty("edison.vault.secret-path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.properties")).thenReturn("keyOne,keyTwo,keyThree");
        when(environment.getProperty("edison.vault.token-source")).thenReturn("file");
        when(environment.getProperty("edison.vault.environment-token")).thenReturn("someEnvVariable");
        when(environment.getProperty("edison.vault.file-token")).thenReturn("someFile");
        when(environment.getProperty("edison.vault.appid")).thenReturn("someAppId");
        when(environment.getProperty("edison.vault.userid")).thenReturn("someUserId");

        // When
        ConfigProperties testee = new ConfigProperties(environment);

        // Then
        assertThat(testee.isEnabled(), is(true));
        assertThat(testee.getBaseUrl(), is("someBaseUrl"));
        assertThat(testee.getSecretPath(), is("someSecretPath"));
        assertThat(testee.getProperties(), Matchers.hasSize(3));
        assertThat(testee.getProperties().get(0), is("keyOne"));
        assertThat(testee.getProperties().get(1), is("keyTwo"));
        assertThat(testee.getProperties().get(2), is("keyThree"));
        assertThat(testee.getTokenSource(), is(VaultTokenReader.TokenSource.file));
        assertThat(testee.getAppId(), is("someAppId"));
        assertThat(testee.getUserId(), is("someUserId"));

    }
}