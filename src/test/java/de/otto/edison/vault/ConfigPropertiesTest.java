package de.otto.edison.vault;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.springframework.core.env.Environment;
import org.testng.annotations.Test;

public class ConfigPropertiesTest {

    @Test
    public void shouldHaveConfigValues() throws Exception {
        // Given
        Environment environment = mock(Environment.class);
        when(environment.getProperty("edison.vault.enabled")).thenReturn("true");
        when(environment.getProperty("edison.vault.base-url")).thenReturn("someBaseUrl");
        when(environment.getProperty("edison.vault.secret-path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.properties")).thenReturn("keyOne.key1, keyOne, keyTwo, keyThree.value, keyFour.key4");
        when(environment.getProperty("edison.vault.token-source")).thenReturn("file");
        when(environment.getProperty("edison.vault.environment-token")).thenReturn("someEnvVariable");
        when(environment.getProperty("edison.vault.file-token")).thenReturn("someFile");
        when(environment.getProperty("edison.vault.appid")).thenReturn("someAppId");
        when(environment.getProperty("edison.vault.userid")).thenReturn("someUserId");
        when(environment.getProperty("edison.vault.token-filename")).thenReturn("someFilename");

        // When
        ConfigProperties testee = new ConfigProperties(environment);

        // Then
        assertThat(testee.isEnabled(), is(true));
        assertThat(testee.getBaseUrl(), is("someBaseUrl"));
        assertThat(testee.getSecretPath(), is("someSecretPath"));
        assertThat(testee.getProperties(), Matchers.hasSize(5));
        assertThat(testee.getProperties(), hasItems("keyOne.key1","keyOne", "keyTwo", "keyThree.value", "keyFour.key4"));
        assertThat(testee.getTokenSource(), is("file"));
        assertThat(testee.getAppId(), is("someAppId"));
        assertThat(testee.getUserId(), is("someUserId"));
        assertThat(testee.getTokenFileName(), is("someFilename"));

    }

    @Test
    public void shouldSetDefaultTokenFileName() throws Exception {
        // Given
        Environment environment = mock(Environment.class);
        when(environment.getProperty("edison.vault.token-filename")).thenReturn(null);

        // When
        ConfigProperties testee = new ConfigProperties(environment);

        // Then
        assertThat(testee.getTokenFileName(), is(".vault-token"));

    }

    @Test
    public void shouldSetDefaultToken() throws Exception {
        // Given
        Environment environment = mock(Environment.class);
        when(environment.getProperty("user.home")).thenReturn("/test");

        // When
        ConfigProperties testee = new ConfigProperties(environment);

        // Then
        assertThat(testee.getDefaultVaultTokenFileName(), is("/test/.vault-token"));

    }
}