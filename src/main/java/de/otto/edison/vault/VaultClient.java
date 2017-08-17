package de.otto.edison.vault;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

public class VaultClient {

    private static final Logger LOG = LoggerFactory.getLogger(VaultClient.class);

    private final String vaultBaseUrl;
    private final String secretPath;
    private final String vaultToken;

    protected AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public static VaultClient vaultClient(final ConfigProperties configProperties, String vaultToken) {
        return vaultClient(configProperties.getBaseUrl(), configProperties.getSecretPath(), vaultToken);
    }

    public static VaultClient vaultClient(final String vaultBaseUrl, final String secretPath, final String vaultToken) {
        return new VaultClient(vaultBaseUrl, secretPath, vaultToken);
    }

    private VaultClient(final String vaultBaseUrl, final String secretPath, final String vaultToken) {
        this.vaultBaseUrl = removeTrailingSlash(vaultBaseUrl);
        this.secretPath = removeLeadingSlash(removeTrailingSlash(secretPath));
        this.vaultToken = vaultToken;
    }

    public Map<String, String> readFields(final String key) {
        try {
            final StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(vaultBaseUrl).append("/v1");

            if (!StringUtils.isEmpty(secretPath)) {
                urlBuilder.append("/").append(secretPath);
            }

            if (!StringUtils.isEmpty(key)) {
                urlBuilder.append("/").append(key);
            }

            final String url = urlBuilder.toString();
            final Response response = asyncHttpClient
                    .prepareGet(url)
                    .setHeader("X-Vault-Token", vaultToken)
                    .execute()
                    .get();
            if ((response.getStatusCode() != 200)) {
                LOG.error("can't read vault property '{}' with token '{}' from url '{}'", key, vaultToken, url);
                throw new RuntimeException(
                        String.format("read of vault property '%s' with token '%s' from url '%s' failed, return code is '%s'",
                                key, vaultToken, url, response.getStatusCode()));
            }
            LOG.info("read of vault property '{}' successful", key);

            return extractFields(response.getResponseBody("utf-8"));
        } catch (ExecutionException | InterruptedException | IOException e) {
            LOG.error(String.format("extract of vault property '%s' failed", key), e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> extractFields(final String responseBody) {
        Map<String, Object> responseMap = new Gson().fromJson(responseBody, Map.class);
        return (Map<String, String>) responseMap.get("data");
    }

    private String removeTrailingSlash(final String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private String removeLeadingSlash(final String url) {
        if (url.startsWith("/")) {
            return url.substring(1);
        }
        return url;
    }
}
