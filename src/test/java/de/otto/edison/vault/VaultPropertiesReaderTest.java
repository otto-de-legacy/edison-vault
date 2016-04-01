package de.otto.edison.vault;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(VaultClient.class)
public class VaultPropertiesReaderTest extends PowerMockTestCase {

    private VaultPropertiesReader testee;
    private VaultTokenFactory vaultTokenFactory;
    private VaultToken vaultToken;
    private Environment environment;
    private VaultClient vaultClient;

    @BeforeMethod
    public void setUp() throws Exception {
        vaultToken = mock(VaultToken.class);
        vaultTokenFactory = mock(VaultTokenFactory.class);
        environment = mock(Environment.class);
        vaultClient = mock(VaultClient.class);

        testee = spy(new VaultPropertiesReader(environment));
    }

    @Test
    public void shouldEnableVaultIfPropertySetToTrue() throws Exception {
        // given
        when(environment.getProperty("edison.vault.enabled")).thenReturn("true");

        // when
        boolean vaultEnabled = testee.vaultEnabled();

        // then
        assertThat(vaultEnabled, is(true));
    }


    @Test
    public void shouldDisableVaultIfPropertySetToFalse() throws Exception {
        // given
        when(environment.getProperty("edison.vault.enabled")).thenReturn("false");

        // when
        boolean vaultEnabled = testee.vaultEnabled();

        // then
        assertThat(vaultEnabled, is(false));
    }

    @Test
    public void shouldDisableVaultIfPropertyNull() throws Exception {
        // given
        when(environment.getProperty("edison.vault.enabled")).thenReturn(null);

        // when
        boolean vaultEnabled = testee.vaultEnabled();

        // then
        assertThat(vaultEnabled, is(false));
    }


    @Test
    public void shouldGetVaultClientFromAppIdAndUserId() throws Exception {
        // given
        testee.vaultTokenFactory = vaultTokenFactory;
        when(environment.getProperty("edison.vault.base-url")).thenReturn("http://someBaseUrl");
        when(environment.getProperty("edison.vault.secret-path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.appid")).thenReturn("someAppId");
        when(environment.getProperty("edison.vault.userid")).thenReturn("someUserId");
        when(vaultTokenFactory.createVaultToken()).thenReturn(vaultToken);

        // when
        testee.getVaultClient();

        // then
        verify(environment).getProperty("edison.vault.base-url");
        verify(environment).getProperty("edison.vault.secret-path");
        verify(environment).getProperty("edison.vault.appid");
        verify(environment).getProperty("edison.vault.userid");
    }

    @Test
    public void shouldGetVaultClientFromEnvironmentToken() throws Exception {
        // given
        when(environment.getProperty("edison.vault.base-url")).thenReturn("http://someBaseUrl");
        when(environment.getProperty("edison.vault.secret-path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.environment-token")).thenReturn("someClientToken");

        // when
        testee.getVaultClient();

        // then
        verify(environment).getProperty("edison.vault.base-url");
        verify(environment).getProperty("edison.vault.secret-path");
        verify(environment).getProperty("edison.vault.appid");
        verify(environment).getProperty("edison.vault.userid");
    }

    @Test
    public void shouldGetVaultAddrFromEnvironmentIfPropertyIsNotSet() throws Exception {
        mockStatic(VaultClient.class);
        // given
        when(environment.getProperty("edison.vault.base-url")).thenReturn("");
        when(environment.getProperty("edison.vault.secret-path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.environment-token")).thenReturn("someClientToken");
        when(VaultClient.getVaultAddrFromEnv()).thenReturn("someBaseurl");

        // when
        testee.getVaultClient();

        // then
        verifyStatic(times(1));
        VaultClient.getVaultAddrFromEnv();
    }

    @Test
    public void shouldReadPropertiesFromVaultAndSetThem() throws Exception {
        // given
        ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);

        when(environment.getProperty("edison.vault.enabled")).thenReturn("true");
        when(environment.getProperty("edison.vault.properties")).thenReturn("someKey1,someKey2");
        when(environment.getProperty("edison.vault.base-url")).thenReturn("http://someBaseUrl");
        when(environment.getProperty("edison.vault.secret-path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.appid")).thenReturn("someAppId");
        when(environment.getProperty("edison.vault.userid")).thenReturn("someUserId");
        when(beanFactory.getBeanDefinitionNames()).thenReturn(new String[]{});
        when(vaultClient.read("someKey1")).thenReturn("someValue1");
        when(vaultClient.read("someKey2")).thenReturn("someValue2");
        doReturn(vaultClient).when(testee).getVaultClient();

        // when
        Properties properties = testee.fetchPropertiesFromVault();

        // then
        Properties expectedProperties = new Properties();
        expectedProperties.setProperty("someKey1", "someValue1");
        expectedProperties.setProperty("someKey2", "someValue2");

        assertThat(properties, is(expectedProperties));
    }


}