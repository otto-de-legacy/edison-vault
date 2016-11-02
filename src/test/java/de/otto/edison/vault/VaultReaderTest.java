package de.otto.edison.vault;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.util.Collections.singletonMap;
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
        Set<String> propertiesToRead = new HashSet<>();
        propertiesToRead.add("someKey1");
        propertiesToRead.add("someKey2");
        propertiesToRead.add("someKey3");

        when(configProperties.getProperties()).thenReturn(propertiesToRead);
        when(configProperties.getPropertyFieldnames("someKey1")).thenReturn(new HashSet<>(Arrays.asList("value")));
        when(configProperties.getPropertyFieldnames("someKey2")).thenReturn(new HashSet<>(Arrays.asList("value")));
        when(configProperties.getPropertyFieldnames("someKey3")).thenReturn(new HashSet<>(Arrays.asList("field3", "field3_2")));
        when(vaultClient.readFields("someKey1")).thenReturn(singletonMap("value", "someValue1"));
        when(vaultClient.readFields("someKey2")).thenReturn(singletonMap("value", "someValue2"));

        final Map<String, String> someKey3Values = new HashMap<>();
        someKey3Values.put("field3", "someValue3");
        someKey3Values.put("field3_2", "someValue3_2");
        when(vaultClient.readFields("someKey3")).thenReturn(someKey3Values);

        // when
        Properties properties = testee.fetchPropertiesFromVault();

        // then
        Properties expectedProperties = new Properties();
        expectedProperties.setProperty("someKey1", "someValue1");
        expectedProperties.setProperty("someKey1@value", "someValue1");
        expectedProperties.setProperty("someKey2", "someValue2");
        expectedProperties.setProperty("someKey2@value", "someValue2");
        expectedProperties.setProperty("someKey3@field3", "someValue3");
        expectedProperties.setProperty("someKey3@field3_2", "someValue3_2");

        assertThat(properties, is(expectedProperties));
    }
}