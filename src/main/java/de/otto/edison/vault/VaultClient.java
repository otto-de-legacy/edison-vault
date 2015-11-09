package de.otto.edison.vault;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class VaultClient {

    private static final Logger LOG = LoggerFactory.getLogger(VaultClient.class);

    private final String vaultBaseUrl;
    private final String appId;
    private final String userId;
    private final String secretPath;

    protected AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public VaultClient(final String vaultBaseUrl, final String secretPath,  final String appId, final String userId) {
        this.vaultBaseUrl = vaultBaseUrl;
        this.secretPath = secretPath;
        this.appId = appId;
        this.userId = userId;
    }

    public String login() {
        try {
            final Response response = asyncHttpClient
                    .preparePost(vaultBaseUrl + "/auth/app-id/login")
                    .setBody(createAuthBody())
                    .execute()
                    .get();

            if ((response.getStatusCode() != 200)) {
                throw new RuntimeException("login to vault failed, return code is " + response.getStatusCode());
            }
            LOG.info("login to vault successful");

            return extractToken(response.getResponseBody());
        } catch (ExecutionException | InterruptedException | IOException e) {
            LOG.error("login to vault failed", e);
            throw new RuntimeException(e);
        }
    }

    public String read(final String clientToken, final String key) {
        try {
            final Response response = asyncHttpClient
                    .prepareGet(vaultBaseUrl + secretPath + "/" + key)
                    .setHeader("X-Vault-Token", clientToken)
                    .execute()
                    .get();
            if ((response.getStatusCode() != 200)) {
                throw new RuntimeException(String.format("read of vault property '%s' failed, return code is '%s'", key, response.getStatusCode()));
            }
            LOG.info("read of vault property '{}' successful", key);

            return extractProperty(response.getResponseBody());
        } catch (ExecutionException | InterruptedException | IOException e) {
            LOG.error(String.format("read of vault property '%s' failed", key), e);
            throw new RuntimeException(e);
        }
    }

    private String extractProperty(final String responseBody) {
        Map<String, Object> responseMap = new Gson().fromJson(responseBody, Map.class);
        Map<String, String> data = (Map<String, String>) responseMap.get("data");

        return data.get("value");
    }

    public void revoke(final String clientToken) {
        try {
            final Response response = asyncHttpClient
                    .preparePost(vaultBaseUrl + "/auth/token/revoke/" + clientToken)
                    .setHeader("X-Vault-Token", clientToken)
                    .execute()
                    .get();
            if (response.getStatusCode() != 204) {
                throw new RuntimeException(String.format("revoke of vault clientToken failed, return code is '%s'", response.getStatusCode()));
            }
            LOG.info("revoke of vault clientToken successful");
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("revoke of vault clientToken failed", e);
            throw new RuntimeException(e);
        }
    }

    private String createAuthBody() {
        return String.format("{\"app_id\":\"%s\", \"user_id\": \"%s\"}", appId, userId);
    }

    private String extractToken(final String responseBody) {
        Map<String, Object> responseMap = new Gson().fromJson(responseBody, Map.class);
        Map<String, String> auth = (Map<String, String>) responseMap.get("auth");

        return auth.get("client_token");
    }
}
