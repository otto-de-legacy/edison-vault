package de.otto.edison.vault;

import com.ning.http.client.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static de.otto.edison.vault.VaultClient.vaultClient;

/**
 * Scans all spring properties after they have been loaded and checks for values beginning with
 * <code>vault://</code>. These are property values which need to be fetched from vault.
 */
@Component
@ConditionalOnProperty(prefix = "edison.vault", name = "enableconfigurer", matchIfMissing = true)
public class VaultPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer
        implements EnvironmentAware, DisposableBean {

    private static final String VAULT_PROPERTY_PREFIX_MARKER = "vault://";

    private static final Logger LOG = LoggerFactory.getLogger(VaultPropertyPlaceholderConfigurer.class);

    private ConfigProperties vaultConfigProperties;

    // we do not want to call vault multiple times for the same value
    // that's why we cache the results from the previous calls
    private ConcurrentHashMap<String, Map<String, String>> vaultProperties = new ConcurrentHashMap<>();

    private boolean isSecretVaultProperty(final String propertyValue) {
        return propertyValue != null && propertyValue.startsWith(VAULT_PROPERTY_PREFIX_MARKER);
    }

    private String lookupVaultValueFromFieldMap(final VaultField fieldInfo, final Map<String, String> vaultFieldValues) {
        final String vaultFieldValue = vaultFieldValues.get(fieldInfo.getVaultFieldName());
        if (vaultFieldValue != null) {
            LOG.info("read of value '{}' from vault property '{}' successful",
                    fieldInfo.getVaultFieldName(),
                    fieldInfo.getVaultSecretPathName());
            return vaultFieldValue;
        } else {
            throw new RuntimeException("unable read value '" + fieldInfo.getVaultFieldName() +
                    " ' from vault property ' " + fieldInfo.getVaultSecretPathName()
                    + "' - value not found");
        }
    }

    String convertVaultProperty(final String springPropertyValue) {

        if (vaultConfigProperties.isEnabled() && isSecretVaultProperty(springPropertyValue)) {
            final VaultField fieldInfo = new VaultField(springPropertyValue);
            final Map<String, String> cachedVaultValues = vaultProperties.get(fieldInfo.getVaultSecretPathName());
            if (cachedVaultValues != null) {
                // lookup cached values
                return lookupVaultValueFromFieldMap(fieldInfo, cachedVaultValues);
            } else {
                // else ask vault
                final VaultClient vaultClient = createVaultClient(vaultConfigProperties);
                final Map<String, String> vaultFieldValues = vaultClient.readFields(fieldInfo.getVaultSecretPathName());
                // cache vaultFieldValues
                vaultProperties.put(fieldInfo.getVaultSecretPathName(), vaultFieldValues);

                return lookupVaultValueFromFieldMap(fieldInfo, vaultFieldValues);
            }
        }
        return springPropertyValue;
    }

    @Override
    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                       final StringValueResolver valueResolver) {
        super.doProcessProperties(beanFactoryToProcess,
                strVal -> convertVaultProperty(valueResolver.resolveStringValue(strVal))
        );
    }

    @Override
    public void setEnvironment(final Environment environment) {
        super.setEnvironment(environment);
        this.vaultConfigProperties = new ConfigProperties(environment);
    }

    @Override
    public void destroy() throws Exception {
        vaultProperties.clear();
    }

    protected VaultClient createVaultClient(final ConfigProperties configProperties) {
        return vaultClient(configProperties, new VaultTokenReader(new AsyncHttpClient()).readVaultToken(configProperties));
    }

    private static class VaultField {

        private final String vaultSecretPathName;
        private final String vaultFieldName;

        VaultField(final String springPropertyValueScheme) {

            final String springPropertyValue = springPropertyValueScheme.replaceFirst("^" + VAULT_PROPERTY_PREFIX_MARKER, "");
            final int lastHashIndex = springPropertyValue.lastIndexOf("#");
            if (lastHashIndex >= 0) {
                this.vaultSecretPathName = springPropertyValue.substring(0, lastHashIndex);
                this.vaultFieldName = springPropertyValue.substring(lastHashIndex + 1);
            } else {
                this.vaultSecretPathName = "";
                this.vaultFieldName = springPropertyValue;
            }
        }

        String getVaultSecretPathName() {
            return vaultSecretPathName;
        }

        String getVaultFieldName() {
            return vaultFieldName;
        }
    }
}
