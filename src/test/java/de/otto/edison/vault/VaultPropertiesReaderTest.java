package de.otto.edison.vault;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class VaultPropertiesReaderTest {

    private VaultPropertiesReader testee;
    private VaultClient vaultClient;

    @BeforeMethod
    public void setUp() throws Exception {
        vaultClient = mock(VaultClient.class);

        testee = spy(new VaultPropertiesReader());
        doReturn(vaultClient).when(testee).getVaultClient();
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
        when(vaultClient.login()).thenReturn("someClientToken");
        when(vaultClient.read("someClientToken", "someKey1")).thenReturn("someValue1");
        when(vaultClient.read("someClientToken", "someKey2")).thenReturn("someValue2");

        // when
        testee.setEnvironment(environment);
        testee.postProcessBeanFactory(beanFactory);

        // then
        verify(vaultClient).login();
        verify(vaultClient).read("someClientToken", "someKey1");
        verify(vaultClient).read("someClientToken", "someKey2");
        verify(vaultClient).revoke("someClientToken");
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
        when(vaultClient.login()).thenReturn("someClientToken");
        when(vaultClient.read("someClientToken", "someKey1")).thenReturn("someValue1");
        when(vaultClient.read("someClientToken", "someKey2")).thenReturn("someValue2");

        // when
        testee.setEnvironment(environment);
        testee.postProcessBeanFactory(beanFactory);

        // then
        verify(vaultClient).login();
        verify(vaultClient).read("someClientToken", "someKey1");
        verify(vaultClient).read("someClientToken", "someKey2");
        verify(vaultClient).revoke("someClientToken");
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
        when(vaultClient.login()).thenReturn("someClientToken");
        when(vaultClient.read("someClientToken", "someKey1")).thenReturn("someValue1");
        when(vaultClient.read("someClientToken", "someKey2")).thenReturn("someValue2");

        // when
        testee.setEnvironment(environment);
        testee.postProcessBeanFactory(beanFactory);

        // then
        verify(vaultClient).login();
        verify(vaultClient).revoke("someClientToken");
        verifyNoMoreInteractions(vaultClient);

        Properties properties = new Properties();
        verify(testee).setProperties(properties);
    }

    @Test
    public void shouldGetVaultClient() throws Exception {
        // given
        testee = new VaultPropertiesReader();
        Environment environment = mock(Environment.class);
        when(environment.getProperty("edison.vault.base.url")).thenReturn("http://someBaseUrl");
        when(environment.getProperty("edison.vault.secret.path")).thenReturn("someSecretPath");
        when(environment.getProperty("edison.vault.app.id")).thenReturn("someAppId");
        when(environment.getProperty("edison.vault.user.id")).thenReturn("someUserId");

        // when
        testee.setEnvironment(environment);
        testee.getVaultClient();

        // then
        verify(environment).getProperty("edison.vault.base.url");
        verify(environment).getProperty("edison.vault.secret.path");
        verify(environment).getProperty("edison.vault.app.id");
        verify(environment).getProperty("edison.vault.user.id");
        verifyNoMoreInteractions(environment);
    }

}