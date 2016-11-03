package de.otto.edison.vault;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigProperties {

    private final boolean enabled;
    private final String baseUrl;
    private final String secretPath;
    private final Set<String> properties;
    private final String tokenSource;
    private final String environmentToken;
    private final String fileToken;
    private final String appId;
    private final String userId;
    private final String defaultVaultToken;

    public ConfigProperties(Environment environment) {
        enabled = Boolean.parseBoolean(environment.getProperty("edison.vault.enabled"));
        final String baseUrlProperty = environment.getProperty("edison.vault.base-url");
        baseUrl = StringUtils.isEmpty(baseUrlProperty) ? getVaultAddrFromEnv() : baseUrlProperty;
        secretPath = environment.getProperty("edison.vault.secret-path");
        properties = splitVaultPropertyKeys(environment.getProperty("edison.vault.properties"));
        tokenSource = environment.getProperty("edison.vault.token-source");
        environmentToken = environment.getProperty("edison.vault.environment-token");
        fileToken = environment.getProperty("edison.vault.file-token");
        appId = environment.getProperty("edison.vault.appid");
        userId = environment.getProperty("edison.vault.userid");
        final String homeDir = environment.getProperty("user.home");
        defaultVaultToken = homeDir + "/.vault-token";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getSecretPath() {
        return secretPath;
    }

    public Set<String> getProperties() {
        return properties;
    }

    public String getTokenSource() {
        return tokenSource;
    }

    public String getEnvironmentToken() {
        return environmentToken;
    }

    public String getFileToken() {
        return fileToken;
    }

    public String getAppId() {
        return appId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDefaultVaultTokenFileName() {
        return defaultVaultToken;
    }

    private static String getVaultAddrFromEnv() {
        return System.getenv("VAULT_ADDR");
    }

    private Set<String> splitVaultPropertyKeys(String properties) {
        if (StringUtils.isEmpty(properties)) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(
                Arrays.stream(properties.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigProperties that = (ConfigProperties) o;

        if (enabled != that.enabled) {
            return false;
        }
        if (baseUrl != null ? !baseUrl.equals(that.baseUrl) : that.baseUrl != null) {
            return false;
        }
        if (secretPath != null ? !secretPath.equals(that.secretPath) : that.secretPath != null) {
            return false;
        }
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) {
            return false;
        }
        if (tokenSource != null ? !tokenSource.equals(that.tokenSource) : that.tokenSource != null) {
            return false;
        }
        if (environmentToken != null ? !environmentToken.equals(that.environmentToken) : that.environmentToken != null) {
            return false;
        }
        if (fileToken != null ? !fileToken.equals(that.fileToken) : that.fileToken != null) {
            return false;
        }
        if (appId != null ? !appId.equals(that.appId) : that.appId != null) {
            return false;
        }
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) {
            return false;
        }
        return defaultVaultToken != null ? defaultVaultToken.equals(that.defaultVaultToken) : that.defaultVaultToken == null;
    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + (baseUrl != null ? baseUrl.hashCode() : 0);
        result = 31 * result + (secretPath != null ? secretPath.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (tokenSource != null ? tokenSource.hashCode() : 0);
        result = 31 * result + (environmentToken != null ? environmentToken.hashCode() : 0);
        result = 31 * result + (fileToken != null ? fileToken.hashCode() : 0);
        result = 31 * result + (appId != null ? appId.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (defaultVaultToken != null ? defaultVaultToken.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigProperties{" +
                "enabled=" + enabled +
                ", baseUrl='" + baseUrl + '\'' +
                ", secretPath='" + secretPath + '\'' +
                ", properties=" + properties +
                ", tokenSource='" + tokenSource + '\'' +
                ", environmentToken='" + environmentToken + '\'' +
                ", fileToken='" + fileToken + '\'' +
                ", appId='" + appId + '\'' +
                ", userId='" + userId + '\'' +
                ", defaultVaultToken='" + defaultVaultToken + '\'' +
                '}';
    }
}
