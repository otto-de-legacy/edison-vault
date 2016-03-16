package de.otto.edison.vault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Properties;

import static de.otto.edison.vault.VaultClient.vaultClient;
import static java.lang.Boolean.parseBoolean;

@Component
public class VaultPropertiesReader extends PropertySourcesPlaceholderConfigurer {

    protected Environment environment;

    private Logger LOG = LoggerFactory.getLogger(VaultPropertiesReader.class);

    protected VaultTokenFactory vaultTokenFactory = new VaultTokenFactory();

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (vaultEnabled()) {
            setProperties(fetchPropertiesFromVault());
        }
        super.postProcessBeanFactory(beanFactory);
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
        super.setEnvironment(environment);
    }

    protected boolean vaultEnabled() {
        final String vaultEnabled = environment.getProperty("edison.vault.enabled");
        return vaultEnabled != null && parseBoolean(vaultEnabled);
    }

    protected VaultClient getVaultClient() {
        final String vaultSecretPath = environment.getProperty("edison.vault.secret-path");
        final String tokenEnvironment = environment.getProperty("edison.vault.environment-token");
        final String tokenFile = environment.getProperty("edison.vault.file-token");
        final String vaultAppId = environment.getProperty("edison.vault.appid");
        final String vaultUserId = environment.getProperty("edison.vault.userid");

        String vaultBaseUrl = environment.getProperty("edison.vault.base-url");

        if (StringUtils.isEmpty(vaultBaseUrl)) {
            vaultBaseUrl = VaultClient.getVaultAddrFromEnv();
        }

        VaultToken vaultToken = vaultTokenFactory.createVaultToken();

        if (!StringUtils.isEmpty(tokenEnvironment)) {
            LOG.info("read token from env variable '{}'", tokenEnvironment);
            vaultToken.readTokenFromEnv(tokenEnvironment);
        } else if (!StringUtils.isEmpty(tokenFile)) {
            LOG.info("read token from file '{}'", tokenFile);
            vaultToken.readTokenFromFile(tokenFile);
        } else {
            LOG.info("get token from login");
            vaultToken.readTokenFromLogin(vaultBaseUrl, vaultAppId, vaultUserId);
        }

        return vaultClient(vaultBaseUrl, vaultSecretPath, vaultToken);
    }

    private Properties fetchPropertiesFromVault() {
        final Properties vaultProperties = new Properties();
        final VaultClient vaultClient = getVaultClient();
        for (final String key : fetchVaultPropertyKeys()) {
            final String trimmedKey = key.trim();
            vaultProperties.setProperty(trimmedKey, vaultClient.read(trimmedKey));
        }
        return vaultProperties;
    }

    private String[] fetchVaultPropertyKeys() {
        final String vaultPropertyKeys = environment.getProperty("edison.vault.properties");
        if (StringUtils.isEmpty(vaultPropertyKeys)) {
            return new String[0];
        }
        return vaultPropertyKeys.split(",");
    }
}
