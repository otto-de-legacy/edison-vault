package de.otto.edison.vault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

/**
 * Adds a new vault property source at the end of all property sources.
 */
@Component
@ConditionalOnProperty(prefix = "edison.vault", name = "enableconfigurer", matchIfMissing = true)
public class VaultPropertySourcePostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    private ConfigProperties configProperties;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
        final MutablePropertySources propertySources = env.getPropertySources();
        propertySources.addLast(new VaultPropertySource("vaultPropertySource", configProperties));
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.configProperties = new ConfigProperties(environment);
    }

}
