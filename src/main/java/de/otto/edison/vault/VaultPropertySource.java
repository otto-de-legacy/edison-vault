package de.otto.edison.vault;

import com.ning.http.client.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.otto.edison.vault.VaultClient.vaultClient;

public class VaultPropertySource extends MapPropertySource {

    private static final Logger LOG = LoggerFactory.getLogger(VaultPropertySource.class);

    public VaultPropertySource(final String name, final ConfigProperties configProperties) {
        super(name, new HashMap<>());
        if (configProperties.isEnabled()) {
            loadPropertiesFromVault(createVaultClient(configProperties), configProperties.getProperties());
        }
    }

    protected VaultClient createVaultClient(final ConfigProperties configProperties) {
        return vaultClient(configProperties, new VaultTokenReader(new AsyncHttpClient()).readVaultToken(configProperties));
    }

    private void loadPropertiesFromVault(final VaultClient vaultClient, Set<String> properties) {
        properties
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
                                    source.put(field.getSpringPropertyPath(), vaultFieldValue);
                                } else {
                                    throw new RuntimeException("unable read value '" + field.getVaultFieldName() +
                                            "' from vault property '" + field.getVaultSecretPathName() + "' - value not found");
                                }
                            });
                        });
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
