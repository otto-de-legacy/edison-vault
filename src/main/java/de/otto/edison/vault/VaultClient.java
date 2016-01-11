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
    private final String secretPath;
    private final VaultToken vaultToken;

    protected AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public static String getVaultAddrFromEnv() {
        return System.getenv("VAULT_ADDR");
    }

    public static VaultClient vaultClient(final String vaultBaseUrl, final String secretPath, final VaultToken vaultToken) {
        return new VaultClient(vaultBaseUrl, secretPath, vaultToken);
    }

    private VaultClient(final String vaultBaseUrl, final String secretPath, final VaultToken vaultToken) {
        this.vaultBaseUrl = vaultBaseUrl;
        this.secretPath = secretPath;
        this.vaultToken = vaultToken;
    }

    public String read(final String key) {
        try {
            final String url = vaultBaseUrl + "/v1" + secretPath + "/" + key;
            final Response response = asyncHttpClient
                    .prepareGet(url)
                    .setHeader("X-Vault-Token", vaultToken.getToken())
                    .execute()
                    .get();
            if ((response.getStatusCode() != 200)) {
                LOG.error("can't read vault property from '{}' with token '{}'", key, vaultToken.getToken());
                throw new RuntimeException(String.format("read of vault property '%s' with token '%s' failed, return code is '%s'", key, response.getStatusCode()));
            }
            LOG.info("read of vault property '{}' successful", key);

            return extractProperty(response.getResponseBody());
        } catch (ExecutionException | InterruptedException | IOException e) {
            LOG.error(String.format("extract of vault property '%s' failed", key), e);
            throw new RuntimeException(e);
        }
    }

    public void revoke() {
        vaultToken.revoke();
    }

    private String extractProperty(final String responseBody) {
        Map<String, Object> responseMap = new Gson().fromJson(responseBody, Map.class);
        Map<String, String> data = (Map<String, String>) responseMap.get("data");

        return data.get("value");
    }

}
