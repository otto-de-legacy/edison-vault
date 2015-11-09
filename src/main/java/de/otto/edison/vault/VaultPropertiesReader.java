package de.otto.edison.vault;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Properties;

import static java.lang.Boolean.parseBoolean;

@Component
public class VaultPropertiesReader extends PropertySourcesPlaceholderConfigurer {

    private Environment environment;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (vaultEnabled()) {
            setProperties(fetchPropertiesFromVault());
        }
        super.postProcessBeanFactory(beanFactory);
    }

    protected boolean vaultEnabled() {
        final String vaultEnabled = environment.getProperty("edison.vault.enabled");
        return vaultEnabled != null && parseBoolean(vaultEnabled);
    }

    private Properties fetchPropertiesFromVault() {
        final Properties vaultProperties = new Properties();

        final VaultClient vaultClient = getVaultClient();
        final String clientToken = vaultClient.login();
        for (final String key : fetchVaultPropertyKeys()) {
            final String trimmedKey = key.trim();
            vaultProperties.setProperty(trimmedKey, vaultClient.read(clientToken, trimmedKey));
        }
        vaultClient.revoke(clientToken);

        return vaultProperties;
    }

    private String[] fetchVaultPropertyKeys() {
        final String vaultPropertyKeys = environment.getProperty("edison.vault.properties");
        if (StringUtils.isEmpty(vaultPropertyKeys)) {
            return new String[0];
        }
        return vaultPropertyKeys.split(",");
    }

    protected VaultClient getVaultClient() {
        final String vaultBaseUrl = environment.getProperty("edison.vault.base.url");
        final String vaultSecretPath = environment.getProperty("edison.vault.secret.path");
        final String vaultAppId = environment.getProperty("edison.vault.app.id");
        final String vaultUserId = environment.getProperty("edison.vault.user.id");

        return new VaultClient(vaultBaseUrl, vaultSecretPath, vaultAppId, vaultUserId);
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
        super.setEnvironment(environment);
    }
}
