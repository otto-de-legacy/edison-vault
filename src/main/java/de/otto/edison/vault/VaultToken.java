package de.otto.edison.vault;

import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class VaultToken {
    private Logger LOG = LoggerFactory.getLogger(VaultToken.class);

    public enum TokenSource {
        undefined, file, login, environment;
    }

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    private final String token;

    public VaultToken(ConfigProperties configProperties, AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
        switch(configProperties.getTokenSource()) {
            case login:
                this.token = readTokenFromLogin(configProperties.getBaseUrl(), configProperties.getAppId(), configProperties.getUserId());
                break;
            case file:
                String fileToken = configProperties.getFileToken();
                if (StringUtils.isEmpty(fileToken)) {
                    fileToken = configProperties.getDefaultVaultTokenFileName();
                 }
                this.token = readTokenFromFile(fileToken);
                break;
            case environment:
                this.token = readTokenFromEnv(configProperties.getEnvironmentToken());
                break;
            default:
                this.token = null;
                break;
        }
    }

    public String getToken() {
        return token;
    }

    protected String readTokenFromFile(String fileName) {
        try {
            File tokenFile = new File(fileName);
            if (! tokenFile.exists() || !tokenFile.canRead()) {
                throw new RuntimeException(String.format("Can not read tokenfile from %s", fileName));
            }
            return new String(Files.readAllBytes(Paths.get(fileName)), "UTF-8").replaceAll("\\s+", "");

        } catch (IOException e) {

        }
        return "";
    }

    public String readTokenFromEnv(final String env) {
        return System.getenv(env);
    }

    public String readTokenFromLogin(final String vaultBaseUrl, final String appId, final String userId) {
        String token;
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
        return token;
    }

    private static String createAuthBody(final String appId, final String userId) {
        return String.format("{\"app_id\":\"%s\", \"user_id\": \"%s\"}", appId, userId);
    }

    private static String extractToken(final String responseBody) {
        Map<String, Object> responseMap = new Gson().fromJson(responseBody, Map.class);
        Map<String, String> auth = (Map<String, String>) responseMap.get("auth");

        return auth.get("client_token");
    }
}
