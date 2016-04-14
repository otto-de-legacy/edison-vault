package de.otto.edison.vault;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(VaultClient.class)
public class VaultReaderTest extends PowerMockTestCase {

    private VaultReader testee;
    private ConfigProperties configProperties;
    private VaultClient vaultClient;

    @BeforeMethod
    public void setUp() throws Exception {
        configProperties = mock(ConfigProperties.class);
        vaultClient = mock(VaultClient.class);
        testee = new VaultReader(configProperties, vaultClient);
    }

    @Test
    public void shouldReadPropertiesFromVaultAndSetThem() throws Exception {
        // given
        List<String> propertiesToRead = new ArrayList();
        propertiesToRead.add("someKey1");
        propertiesToRead.add("someKey2");

        when(configProperties.getProperties()).thenReturn(propertiesToRead);
        when(vaultClient.read("someKey1")).thenReturn("someValue1");
        when(vaultClient.read("someKey2")).thenReturn("someValue2");

        // when
        Properties properties = testee.fetchPropertiesFromVault();

        // then
        Properties expectedProperties = new Properties();
        expectedProperties.setProperty("someKey1", "someValue1");
        expectedProperties.setProperty("someKey2", "someValue2");

        assertThat(properties, is(expectedProperties));
    }


}