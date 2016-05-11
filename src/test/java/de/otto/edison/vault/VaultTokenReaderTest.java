package de.otto.edison.vault;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.testng.Assert.fail;

@PrepareForTest(VaultTokenReader.class)
public class VaultTokenReaderTest extends PowerMockTestCase {

    private ConfigProperties configProperties;
    private AsyncHttpClient asyncHttpClient;

    @BeforeMethod
    public void setUp() throws Exception {
        configProperties = mock(ConfigProperties.class);
        asyncHttpClient = mock(AsyncHttpClient.class);
    }

    @Test
    public void shouldGetTokenFromSystemEnvironment() throws Exception {
        // given
        mockStatic(System.class);
        when(configProperties.getTokenSource()).thenReturn(VaultTokenReader.TokenSource.environment);
        when(configProperties.getEnvironmentToken()).thenReturn("SOME_SYSTEM_ENV_VARIABLE");
        when(System.getenv("SOME_SYSTEM_ENV_VARIABLE")).thenReturn("mySecretAccessToken");

        // when
        // then
        assertThat(new VaultTokenReader(asyncHttpClient).readVaultToken(configProperties), is("mySecretAccessToken"));
        verifyStatic();
        System.getenv("SOME_SYSTEM_ENV_VARIABLE");
    }

    @Test
    public void shouldReadTokenFromLogin() throws Exception {
        // given
        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture listenableFuture = mock(ListenableFuture.class);

        when(response.getResponseBody()).thenReturn(createValidLoginJson("someClientToken"));
        when(response.getStatusCode()).thenReturn(200);
        when(asyncHttpClient.preparePost("http://someBaseUrl/v1/auth/app-id/login")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setBody("{\"app_id\":\"someAppId\", \"user_id\": \"someUserId\"}")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        when(configProperties.getTokenSource()).thenReturn(VaultTokenReader.TokenSource.login);
        when(configProperties.getBaseUrl()).thenReturn("http://someBaseUrl");
        when(configProperties.getAppId()).thenReturn("someAppId");
        when(configProperties.getUserId()).thenReturn("someUserId");

        // then
        assertThat(new VaultTokenReader(asyncHttpClient).readVaultToken(configProperties), is("someClientToken"));
    }

    @Test
    public void shouldThrowRuntimeExceptionIfLoginFails() throws Exception {
        // given
        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture listenableFuture = mock(ListenableFuture.class);

        when(response.getResponseBody()).thenReturn(null);
        when(response.getStatusCode()).thenReturn(401);
        when(asyncHttpClient.preparePost("http://someBaseUrl/v1/auth/app-id/login")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setBody("{\"app_id\":\"someAppId\", \"user_id\": \"someUserId\"}")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        try {
            when(configProperties.getTokenSource()).thenReturn(VaultTokenReader.TokenSource.login);
            when(configProperties.getBaseUrl()).thenReturn("http://someBaseUrl");
            when(configProperties.getAppId()).thenReturn("someAppId");
            when(configProperties.getUserId()).thenReturn("someUserId");
            new VaultTokenReader(asyncHttpClient).readVaultToken(configProperties);
            fail();
        } catch (RuntimeException e) {
            // then
            assertThat(e.getMessage(), is("login to vault failed, return code is 401"));
        }
    }

    @Test
    public void shouldReadTokenFromFile() throws Exception {
        String tokenFileName = "./someTestFile";
        try {
            createTokenFile(tokenFileName, "2434c862-c01c-4bdc-e862-9ba9afceab32");
            when(configProperties.getTokenSource()).thenReturn(VaultTokenReader.TokenSource.file);
            when(configProperties.getFileToken()).thenReturn(tokenFileName);

            assertThat(new VaultTokenReader(asyncHttpClient).readVaultToken(configProperties), is("2434c862-c01c-4bdc-e862-9ba9afceab32"));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            fail();
        } finally {
            new File(tokenFileName).delete();
        }
    }

    @Test
    public void shouldReadTokenFromFileEvenIfThereIsWhiteSpace() throws Exception {
        // Given
        String tokenFileName = "./someTestFile";
        try {
            createTokenFile(tokenFileName, " 2434c862-c01c-4bdc-e862-9ba9afceab32 \n");

            // when
            String token = new VaultTokenReader(asyncHttpClient).readTokenFromFile(tokenFileName);

            // then
            assertThat(token, is("2434c862-c01c-4bdc-e862-9ba9afceab32"));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            fail();
        } finally {
            new File(tokenFileName).delete();
        }
    }

    private void createTokenFile(String fileName, String content) throws IOException {
        Files.writeFile(content, new File(fileName));
    }

    private String createValidLoginJson(String clientToken) {
        return "{\n" +
                "  \"lease_id\": \"\",\n" +
                "  \"renewable\": false,\n" +
                "  \"lease_duration\": 0,\n" +
                "  \"data\": null,\n" +
                "  \"auth\": {\n" +
                "    \"client_token\": \"" + clientToken + "\",\n" +
                "    \"policies\": [\"root\"],\n" +
                "    \"lease_duration\": 0,\n" +
                "    \"renewable\": false,\n" +
                "    \"metadata\": {\n" +
                "      \"app-id\": \"sha1:1c0401b419280b0771d006bcdae683989086a00e\",\n" +
                "      \"user-id\": \"sha1:4dbf74fce71648d54c42e28ad193253600853ca6\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
    }


}