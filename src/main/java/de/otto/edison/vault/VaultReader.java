package de.otto.edison.vault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class VaultReader {

    private static final Logger LOG = LoggerFactory.getLogger(VaultReader.class);

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
        configProperties.getProperties()
                .stream()
                .map(VaultFieldInfo::new)
                .collect(Collectors.groupingBy(VaultFieldInfo::getVaultSecretPathName))
                .forEach(
                        (vaultSecretPath, fields) -> {
                            final Map<String, String> vaultFieldValues = vaultClient.readFields(vaultSecretPath);
                            fields.forEach(field -> {
                                final String vaultFieldValue = vaultFieldValues.get(field.getVaultFieldName());
                                if (vaultFieldValue != null) {
                                    LOG.info("read of value '{}' from vault property '{}' successful",
                                            field.getVaultFieldName(),
                                            field.getVaultSecretPathName());
                                    vaultProperties.put(field.getSpringPropertyPath(), vaultFieldValue);
                                } else {
                                    throw new RuntimeException("unable read value '" + field.getVaultFieldName() +
                                            " ' from vault property ' " + field.getVaultSecretPathName() + "' - value not found");
                                }
                            });
                        });
        return vaultProperties;
    }

    private static class VaultFieldInfo {

        private final String vaultSecretPathName;
        private final String vaultFieldName;
        private final String springPropertyPath;

        VaultFieldInfo(String springPropertyPath) {
            final int lastDotIndex = springPropertyPath.lastIndexOf(".");
            if (lastDotIndex >= 0) {
                this.vaultSecretPathName = springPropertyPath.substring(0, lastDotIndex).replace(".", "/");
                this.vaultFieldName = springPropertyPath.substring(lastDotIndex + 1);
            } else {
                this.vaultSecretPathName = "";
                this.vaultFieldName = springPropertyPath;
            }
            this.springPropertyPath = springPropertyPath;
        }

        String getVaultSecretPathName() {
            return vaultSecretPathName;
        }

        String getVaultFieldName() {
            return vaultFieldName;
        }

        String getSpringPropertyPath() {
            return springPropertyPath;
        }
    }
}
