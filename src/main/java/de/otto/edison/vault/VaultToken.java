package de.otto.edison.vault;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class VaultToken {
    private Logger LOG = LoggerFactory.getLogger(VaultToken.class);
    private final String vaultBaseUrl;

    protected AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    protected String token;

    public VaultToken(final String vaultBaseUrl) {
        this.vaultBaseUrl = vaultBaseUrl;
    }

    protected void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void readTokenFromEnv(final String env) {
        token = System.getenv(env);
    }

    public void readTokenFromLogin(final String vaultBaseUrl, final String appId, final String userId) {
        try {
            final Response response = asyncHttpClient
                    .preparePost(vaultBaseUrl + "/v1/auth/app-id/login")
                    .setBody(createAuthBody(appId, userId))
                    .execute()
                    .get();

            if ((response.getStatusCode() != 200)) {
                throw new RuntimeException("login to vault failed, return code is " + response.getStatusCode());
            }
            LOG.info("login to vault successful");

            token = extractToken(response.getResponseBody());
        } catch (ExecutionException | InterruptedException | IOException e) {
            token = null;
            LOG.error("could not retrieve token from vault", e);
            throw new RuntimeException(e);
        }
    }

    public void revoke() {
        if (token != null ) {
            try {
                final Response response = asyncHttpClient
                        .prepareGet(vaultBaseUrl + "/v1/auth/token/revoke-self")
                        .setHeader("X-Vault-Token", token)
                        .execute()
                        .get();
                if (response.getStatusCode() != 204) {
                    throw new RuntimeException(String.format("revoke of vault clientToken failed, return code is '%s'", response.getStatusCode()));
                }
                LOG.info("revoke of vault clientToken successful");
                token = null;
            } catch (ExecutionException | InterruptedException e) {
                LOG.error("revoke of vault clientToken failed", e);
                throw new RuntimeException(e);
            }
        } else {
            LOG.warn("revoke() called without token");
        }
    }

    private String createAuthBody(final String appId, final String userId) {
        return String.format("{\"app_id\":\"%s\", \"user_id\": \"%s\"}", appId, userId);
    }

    private String extractToken(final String responseBody) {
        Map<String, Object> responseMap = new Gson().fromJson(responseBody, Map.class);
        Map<String, String> auth = (Map<String, String>) responseMap.get("auth");

        return auth.get("client_token");
    }

}
