package de.otto.edison.vault;

import com.ning.http.client.AsyncHttpClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static de.otto.edison.vault.VaultClient.vaultClient;

@Component
@ConditionalOnProperty(prefix = "edison.vault", name = "enableconfigurer", matchIfMissing = true)
public class VaultPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    protected Environment environment;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        ConfigProperties configProperties = new ConfigProperties(environment);
        if (configProperties.isEnabled()) {
            VaultReader vaultReader = new VaultReader(configProperties, vaultClient(configProperties, new VaultToken(configProperties, new AsyncHttpClient())));
            setProperties(vaultReader.fetchPropertiesFromVault());
        }
        super.postProcessBeanFactory(beanFactory);
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
        super.setEnvironment(environment);
    }


}
