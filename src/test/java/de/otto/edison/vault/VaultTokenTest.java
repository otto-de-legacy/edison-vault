package de.otto.edison.vault;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.testng.Assert.fail;

@PrepareForTest(VaultToken.class)
public class VaultTokenTest extends PowerMockTestCase {

    private AsyncHttpClient asyncHttpClient;
    private VaultToken testee;

    @BeforeMethod
    public void setUp() throws Exception {
        asyncHttpClient = mock(AsyncHttpClient.class);
        testee = new VaultToken();
        testee.asyncHttpClient = asyncHttpClient;
    }

    @Test(enabled = true)
    public void shouldGetTokenFromSystemEnvironment() throws Exception {
        // given
        mockStatic(System.class);
        when(System.getenv("SOME_SYSTEM_ENV_VARIABLE")).thenReturn("mySecretAccessToken");

        // when
        testee.readTokenFromEnv("SOME_SYSTEM_ENV_VARIABLE");

        // then
        assertThat(testee.getToken(), is("mySecretAccessToken"));
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
        testee.readTokenFromLogin("http://someBaseUrl", "someAppId", "someUserId");

        // then
        assertThat(testee.getToken(), is("someClientToken"));
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
            testee.readTokenFromLogin("http://someBaseUrl", "someAppId", "someUserId");
            fail();
        } catch (RuntimeException e) {
            // then
            assertThat(e.getMessage(), is("login to vault failed, return code is 401"));
        }
    }

    @Test
    public void shouldReadTokenFromFile() throws Exception {
        // Given
        String tokenFileName = "./someTestFile";
        try {
            createTokenFile(tokenFileName, "2434c862-c01c-4bdc-e862-9ba9afceab32");

            // when
            testee.readTokenFromFile(tokenFileName);

            // then
            assertThat(testee.getToken(), is("2434c862-c01c-4bdc-e862-9ba9afceab32"));
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
            testee.readTokenFromFile(tokenFileName);

            // then
            assertThat(testee.getToken(), is("2434c862-c01c-4bdc-e862-9ba9afceab32"));
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