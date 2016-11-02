package de.otto.edison.vault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class VaultReader {

    private final ConfigProperties configProperties;
    private final VaultClient vaultClient;

    public VaultReader(final ConfigProperties configProperties, VaultClient vaultClient) {
        this.configProperties = configProperties;
        this.vaultClient = vaultClient;
    }

    public boolean vaultEnabled() {
        return configProperties.isEnabled();
    }

    public Properties fetchPropertiesFromVault() {
        final Properties vaultProperties = new Properties();
        configProperties.getProperties().forEach(key ->
                configProperties.getPropertyFieldnames(key).forEach(field -> {
                    final Map<String, String> fields = vaultClient.readFields(key);
                    final String fieldValue = fields.get(field);
                    if (field.equals("value")) {
                        vaultProperties.setProperty(key, fieldValue);
                    }
                    vaultProperties.setProperty(key + "@" + field, fieldValue);
                }));
        return vaultProperties;
    }
}
