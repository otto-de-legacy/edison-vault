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
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest(VaultClient.class)
public class VaultPropertiesReaderTest extends PowerMockTestCase {

    private VaultPropertiesReader testee;
    private VaultClient vaultClient;
    private VaultTokenFactory vaultTokenFactory;
    private VaultToken vaultToken;

    @BeforeMethod
    public void setUp() throws Exception {
        vaultClient = mock(VaultClient.class);
        vaultToken = mock(VaultToken.class);
        vaultTokenFactory = mock(VaultTokenFactory.class);

        testee = spy(new VaultPropertiesReader());
    }

    @Test
    public void shouldEnableVaultIfPropertySetToTrue() throws Exception {
        // given
        Environment environment = mock(Environment.class);
        when(environment.getProperty("edison.vault.enabled")).thenReturn("true");

        // when
        testee.setEnvironment(environment);
        boolean vaultEnabled = testee.vaultEnabled();

        // then
        assertThat(vaultEnabled, is(true));
    }


    @Test
    public void shouldDisableVaultIfPropertySetToFalse() throws Exception {
        // given
        Environment environment = mock(Environment.class);
        when(environment.getProperty("edison.vault.enabled")).thenReturn("false");

        // when
        testee.setEnvironment(environment);
        boolean vaultEnabled = testee.vaultEnabled();

        // then
        assertThat(vaultEnabled, is(false));
    }

    @Test
    public void shouldDisableVaultIfPropertyNull() throws Exception {
        // given
        Environment environment = mock(Environment.class);
        when(environment.getProperty("edison.vault.enabled")).thenReturn(null);

        // when
        testee.setEnvironment(environment);
        boolean vaultEnabled = testee.vaultEnabled();

        // then
        assertThat(vaultEnabled, is(false));
    }

    @Test
    public void shouldReadPropertiesFromVaultAndSetThem() throws Exception {
        // given
        Environment environment = mock(Environment.class);
        ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);

        when(environment.getProperty("edison.vault.enabled")).thenReturn("true");
        when(environment.getProperty("edison.vault.properties")).thenReturn("someKey1,someKey2");
        when(environment.getProperty("edison.vault.base.url")).thenReturn("http://someBaseUrl");
        when(environment.getProperty("edison.vault.secret.path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.app.id")).thenReturn("someAppId");
        when(environment.getProperty("edison.vault.user.id")).thenReturn("someUserId");
        when(beanFactory.getBeanDefinitionNames()).thenReturn(new String[]{});
        when(vaultClient.read("someKey1")).thenReturn("someValue1");
        when(vaultClient.read("someKey2")).thenReturn("someValue2");

        doReturn(vaultClient).when(testee).getVaultClient();


        // when
        testee.setEnvironment(environment);
        testee.postProcessBeanFactory(beanFactory);

        // then
        verify(vaultClient).read("someKey1");
        verify(vaultClient).read("someKey2");
        verify(vaultClient).revoke();
        verifyNoMoreInteractions(vaultClient);

        Properties properties = new Properties();
        properties.setProperty("someKey1", "someValue1");
        properties.setProperty("someKey2", "someValue2");
        verify(testee).setProperties(properties);
    }

    @Test
    public void shouldReadPropertiesWithWhitespaceFromVaultAndSetThem() throws Exception {
        // given
        Environment environment = mock(Environment.class);
        ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);

        when(environment.getProperty("edison.vault.enabled")).thenReturn("true");
        when(environment.getProperty("edison.vault.properties")).thenReturn("  someKey1  ,   someKey2");
        when(environment.getProperty("edison.vault.base.url")).thenReturn("http://someBaseUrl");
        when(environment.getProperty("edison.vault.secret.path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.app.id")).thenReturn("someAppId");
        when(environment.getProperty("edison.vault.user.id")).thenReturn("someUserId");
        when(beanFactory.getBeanDefinitionNames()).thenReturn(new String[]{});
        when(vaultClient.read("someKey1")).thenReturn("someValue1");
        when(vaultClient.read("someKey2")).thenReturn("someValue2");

        doReturn(vaultClient).when(testee).getVaultClient();

        // when
        testee.setEnvironment(environment);
        testee.postProcessBeanFactory(beanFactory);

        // then
        verify(vaultClient).read("someKey1");
        verify(vaultClient).read("someKey2");
        verify(vaultClient).revoke();
        verifyNoMoreInteractions(vaultClient);

        Properties properties = new Properties();
        properties.setProperty("someKey1", "someValue1");
        properties.setProperty("someKey2", "someValue2");
        verify(testee).setProperties(properties);
    }

    @Test
    public void shouldNotReadPropertiesIfNoPropertiesAreDefined() throws Exception {
        // given
        Environment environment = mock(Environment.class);
        ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);

        when(environment.getProperty("edison.vault.enabled")).thenReturn("true");
        when(environment.getProperty("edison.vault.properties")).thenReturn(null);
        when(environment.getProperty("edison.vault.base.url")).thenReturn("http://someBaseUrl");
        when(environment.getProperty("edison.vault.secret.path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.app.id")).thenReturn("someAppId");
        when(environment.getProperty("edison.vault.user.id")).thenReturn("someUserId");
        when(beanFactory.getBeanDefinitionNames()).thenReturn(new String[]{});
        when(vaultClient.read("someKey1")).thenReturn("someValue1");
        when(vaultClient.read("someKey2")).thenReturn("someValue2");

        doReturn(vaultClient).when(testee).getVaultClient();

        // when
        testee.setEnvironment(environment);
        testee.postProcessBeanFactory(beanFactory);

        // then
        verify(vaultClient).revoke();
        verifyNoMoreInteractions(vaultClient);

        Properties properties = new Properties();
        verify(testee).setProperties(properties);
    }

    @Test
    public void shouldGetVaultClientFromAppIdAndUserId() throws Exception {
        // given
        testee.vaultTokenFactory = vaultTokenFactory;
        Environment environment = mock(Environment.class);
        when(environment.getProperty("edison.vault.base.url")).thenReturn("http://someBaseUrl");
        when(environment.getProperty("edison.vault.secret.path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.app.id")).thenReturn("someAppId");
        when(environment.getProperty("edison.vault.user.id")).thenReturn("someUserId");
        when(vaultTokenFactory.createVaultToken("http://someBaseUrl")).thenReturn(vaultToken);

        // when
        testee.setEnvironment(environment);
        testee.getVaultClient();

        // then
        verify(environment).getProperty("edison.vault.base.url");
        verify(environment).getProperty("edison.vault.secret.path");
        verify(environment).getProperty("edison.vault.app.id");
        verify(environment).getProperty("edison.vault.user.id");
    }

    @Test
    public void shouldGetVaultClientFromEnvironmentToken() throws Exception {
        // given
        Environment environment = mock(Environment.class);
        when(environment.getProperty("edison.vault.base.url")).thenReturn("http://someBaseUrl");
        when(environment.getProperty("edison.vault.secret.path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.environment-token")).thenReturn("someClientToken");

        // when
        testee.setEnvironment(environment);
        testee.getVaultClient();

        // then
        verify(environment).getProperty("edison.vault.base.url");
        verify(environment).getProperty("edison.vault.secret.path");
        verify(environment).getProperty("edison.vault.app.id");
        verify(environment).getProperty("edison.vault.user.id");
    }

}