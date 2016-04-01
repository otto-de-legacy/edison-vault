package de.otto.edison.vault;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix ="edison.vault", name = "enableconfigurer", matchIfMissing = true)
public class VaultPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    protected Environment environment;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        VaultPropertiesReader vaultPropertiesReader = new VaultPropertiesReader(environment);
        if (vaultPropertiesReader.vaultEnabled()) {
            setProperties(vaultPropertiesReader.fetchPropertiesFromVault());
        }
        super.postProcessBeanFactory(beanFactory);
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
        super.setEnvironment(environment);
    }


}
