package de.otto.edison.vault;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;

public class VaultTokenReader {
    private static final Logger LOG = LoggerFactory.getLogger(VaultTokenReader.class);

    private final AsyncHttpClient asyncHttpClient;

    public VaultTokenReader(final AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    public String readVaultToken(ConfigProperties configProperties) {
        if(configProperties.getTokenSource() == null) {
            throw new IllegalArgumentException("tokenSource not set");
        }
        switch (configProperties.getTokenSource()) {
            case "login":
                return readTokenFromLogin(configProperties.getBaseUrl(), configProperties.getAppId(), configProperties.getUserId());
            case "file":
                String fileToken = configProperties.getFileToken();
                if (StringUtils.isEmpty(fileToken)) {
                    fileToken = configProperties.getDefaultVaultTokenFileName();
                }
                return readTokenFromFile(fileToken);
            case "environment":
                return readTokenFromEnv(configProperties.getEnvironmentToken());
            default:
                throw new IllegalArgumentException("tokenSource is undefined");
        }
    }

    protected String readTokenFromFile(String fileName) {
        try {
            File tokenFile = new File(fileName);
            if (!tokenFile.exists() || !tokenFile.canRead()) {
                throw new RuntimeException(String.format("Can not read tokenfile from %s", fileName));
            }
            return new String(Files.readAllBytes(Paths.get(fileName)), "UTF-8").replaceAll("\\s+", "");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String readTokenFromEnv(final String env) {
        return System.getenv(env);
    }

    public String readTokenFromLogin(final String vaultBaseUrl, final String appId, final String userId) {
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

            return extractToken(response.getResponseBody());
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("could not retrieve token from vault", e);
            throw new RuntimeException(e);
        }
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
