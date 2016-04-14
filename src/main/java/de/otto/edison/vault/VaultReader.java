package de.otto.edison.vault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class VaultReader {

    private final ConfigProperties configProperties;
    private final VaultClient vaultClient;

    private Logger LOG = LoggerFactory.getLogger(VaultReader.class);

    public VaultReader(final ConfigProperties configProperties, VaultClient vaultClient) {
        this.configProperties = configProperties;
        this.vaultClient = vaultClient;
    }

    public boolean vaultEnabled() {
        return configProperties.isEnabled();
    }

    public Properties fetchPropertiesFromVault() {
        final Properties vaultProperties = new Properties();
        configProperties.getProperties().forEach(key -> {
            final String trimmedKey = key.trim();
            vaultProperties.setProperty(trimmedKey, vaultClient.read(trimmedKey));
        });
        return vaultProperties;
    }

}
