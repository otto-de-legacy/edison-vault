package de.otto.edison.vault;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

public class VaultClientTest {

    private VaultClient testee;
    private AsyncHttpClient asyncHttpClient;

    @BeforeMethod
    public void setUp() throws Exception {
        asyncHttpClient = mock(AsyncHttpClient.class);

        testee = new VaultClient("http://someBaseUrl", "/develop/p13n", "someAppId", "someUserId");
        testee.asyncHttpClient = asyncHttpClient;
    }

    @Test
    public void shouldLogin() throws Exception {
        // given
        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture listenableFuture = mock(ListenableFuture.class);

        when(response.getResponseBody()).thenReturn(createValidLoginJson("someClientToken"));
        when(response.getStatusCode()).thenReturn(200);
        when(asyncHttpClient.preparePost("http://someBaseUrl/auth/app-id/login")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setBody("{\"app_id\":\"someAppId\", \"user_id\": \"someUserId\"}")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        String clientToken = testee.login();

        // then
        assertThat(clientToken, is("someClientToken"));
    }

    @Test
    public void shouldThrowRuntimeExceptionIfLoginFails() throws Exception {
        // given
        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture listenableFuture = mock(ListenableFuture.class);

        when(response.getResponseBody()).thenReturn(null);
        when(response.getStatusCode()).thenReturn(401);
        when(asyncHttpClient.preparePost("http://someBaseUrl/auth/app-id/login")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setBody("{\"app_id\":\"someAppId\", \"user_id\": \"someUserId\"}")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        try {
            testee.login();
            fail();
        } catch (RuntimeException e) {
            // then
            assertThat(e.getMessage(), is("login to vault failed, return code is 401"));
        }
    }

    @Test
    public void shouldReadProperty() throws Exception {
        // given
        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture listenableFuture = mock(ListenableFuture.class);

        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn(createReadResponse("someKey", "someValue"));
        when(asyncHttpClient.prepareGet("http://someBaseUrl/develop/p13n/someKey")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setHeader("X-Vault-Token", "someClientToken")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        String propertyValue = testee.read("someClientToken", "someKey");

        // then
        assertThat(propertyValue, is("someValue"));
    }

    private String createReadResponse(final String key, final String value) {
        return "{\"lease_id\":\"develop/p13n/" + key + "/b74f148e-12de-dbfb-b03f-c950c587e8ea\",\"renewable\":false,\"lease_duration\":2592000,\"data\":{\"value\":\"" + value + "\"},\"auth\":null}";
    }

    @Test
    public void shouldThrowRuntimeExceptionIfReadFails() throws Exception {
        // given
        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture listenableFuture = mock(ListenableFuture.class);

        when(response.getResponseBody()).thenReturn(null);
        when(response.getStatusCode()).thenReturn(500);
        when(asyncHttpClient.prepareGet("http://someBaseUrl/develop/p13n/someKey")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setHeader("X-Vault-Token", "someClientToken")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        try {
            testee.read("someClientToken", "someKey");
            fail();
        } catch (RuntimeException e) {
            // then
            assertThat(e.getMessage(), is("read of vault property 'someKey' failed, return code is '500'"));
        }
    }

    @Test
    public void shouldRevokeClientToken() throws Exception {
        // given
        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture listenableFuture = mock(ListenableFuture.class);

        when(response.getStatusCode()).thenReturn(204);
        when(asyncHttpClient.preparePost("http://someBaseUrl/auth/token/revoke/someClientToken")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setHeader("X-Vault-Token", "someClientToken")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when/then
        testee.revoke("someClientToken");
    }

    @Test
    public void shouldThrowRuntimeExceptionIfRevokeFails() throws Exception {
        // given
        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture listenableFuture = mock(ListenableFuture.class);

        when(response.getStatusCode()).thenReturn(503);
        when(asyncHttpClient.preparePost("http://someBaseUrl/auth/token/revoke/someClientToken")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setHeader("X-Vault-Token", "someClientToken")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        try {
            testee.revoke("someClientToken");
            fail();
        } catch (RuntimeException e) {
            // then
            assertThat(e.getMessage(), is("revoke of vault clientToken failed, return code is '503'"));
        }
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
