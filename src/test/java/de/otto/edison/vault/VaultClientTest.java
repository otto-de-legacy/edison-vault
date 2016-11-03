package de.otto.edison.vault;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static de.otto.edison.vault.VaultClient.vaultClient;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.fail;

public class VaultClientTest {

    private VaultClient testee;
    private AsyncHttpClient asyncHttpClient;
    private ConfigProperties configProperties;

    @BeforeMethod
    public void setUp() throws Exception {
        asyncHttpClient = mock(AsyncHttpClient.class);
        configProperties = mock(ConfigProperties.class);
    }

    @Test
    public void shouldReadTheDefaultFieldValue() throws Exception {
        // given
        when(configProperties.getBaseUrl()).thenReturn("http://someBaseUrl");
        when(configProperties.getSecretPath()).thenReturn("/someSecretPath");

        testee = vaultClient(configProperties, "someClientToken");
        testee.asyncHttpClient = asyncHttpClient;

        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture<Response> listenableFuture = mock(ListenableFuture.class);

        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn(createReadResponse("someKey", "value", "someValue"));
        when(asyncHttpClient.prepareGet(eq("http://someBaseUrl/v1/someSecretPath/someKey"))).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setHeader(eq("X-Vault-Token"), eq("someClientToken"))).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        String propertyValue = testee.readFields("someKey").get("value");

        // then
        assertThat(propertyValue, is("someValue"));
    }

    @Test
    public void shouldReturnNullIfNoFieldValueExists() throws Exception {
        // given
        when(configProperties.getBaseUrl()).thenReturn("http://someBaseUrl");
        when(configProperties.getSecretPath()).thenReturn("/someSecretPath");

        testee = vaultClient(configProperties, "someClientToken");
        testee.asyncHttpClient = asyncHttpClient;

        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture<Response> listenableFuture = mock(ListenableFuture.class);

        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn(createReadResponse("someKey", "someField", "someValue"));
        when(asyncHttpClient.prepareGet(eq("http://someBaseUrl/v1/someSecretPath/someKey"))).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setHeader(eq("X-Vault-Token"), eq("someClientToken"))).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        String fieldValue = testee.readFields("someKey").get("value");

        // then
        assertThat(fieldValue, is(nullValue()));
    }

    @Test
    public void shouldReadAnArbitraryField() throws Exception {
        // given
        when(configProperties.getBaseUrl()).thenReturn("http://someBaseUrl");
        when(configProperties.getSecretPath()).thenReturn("/someSecretPath");

        testee = vaultClient(configProperties, "someClientToken");
        testee.asyncHttpClient = asyncHttpClient;

        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture<Response> listenableFuture = mock(ListenableFuture.class);

        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn(createReadResponse("someKey", "someFieldOtherThanValue", "someValue"));
        when(asyncHttpClient.prepareGet(eq("http://someBaseUrl/v1/someSecretPath/someKey"))).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setHeader(eq("X-Vault-Token"), eq("someClientToken"))).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        String fieldValue = testee.readFields("someKey").get("someFieldOtherThanValue");

        // then
        assertThat(fieldValue, notNullValue());
        assertThat(fieldValue, is("someValue"));
    }

    @Test
    public void shouldReturnEmptyOptionalForANonExistingField() throws Exception {
        // given
        when(configProperties.getBaseUrl()).thenReturn("http://someBaseUrl");
        when(configProperties.getSecretPath()).thenReturn("/someSecretPath");

        testee = vaultClient(configProperties, "someClientToken");
        testee.asyncHttpClient = asyncHttpClient;

        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture<Response> listenableFuture = mock(ListenableFuture.class);

        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn(createReadResponse("someKey", "someField", "someValue"));
        when(asyncHttpClient.prepareGet(eq("http://someBaseUrl/v1/someSecretPath/someKey"))).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setHeader(eq("X-Vault-Token"), eq("someClientToken"))).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        String fieldValue = testee.readFields("someKey").get("someUnknownField");

        // then
        assertThat(fieldValue, nullValue());
    }

    private String createReadResponse(final String key, final String field, final String value) {
        return "{\"lease_id\":\"develop/p13n/" + key + "/b74f148e-12de-dbfb-b03f-c950c587e8ea\",\"renewable\":false,\"lease_duration\":2592000,\"data\":{\"" + field + "\":\"" + value + "\"},\"auth\":null}";
    }

    @Test
    public void shouldThrowRuntimeExceptionIfReadFails() throws Exception {
        // given
        when(configProperties.getBaseUrl()).thenReturn("http://someBaseUrl");
        when(configProperties.getSecretPath()).thenReturn("/someSecretPath");

        testee = vaultClient(configProperties, "someClientToken");
        testee.asyncHttpClient = asyncHttpClient;

        Response response = mock(Response.class);
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilder = mock(AsyncHttpClient.BoundRequestBuilder.class);
        ListenableFuture<Response> listenableFuture = mock(ListenableFuture.class);

        when(response.getResponseBody()).thenReturn(null);
        when(response.getStatusCode()).thenReturn(500);
        when(asyncHttpClient.prepareGet("http://someBaseUrl/v1/someSecretPath/someKey")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setHeader("X-Vault-Token", "someClientToken")).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(listenableFuture);
        when(listenableFuture.get()).thenReturn(response);

        // when
        try {
            testee.readFields("someKey");
            fail();
        } catch (RuntimeException e) {
            // then
            assertThat(e.getMessage(), is("read of vault property 'someKey' with token 'someClientToken' from url 'http://someBaseUrl/v1/someSecretPath/someKey' failed, return code is '500'"));
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldTrimUrlSlashes() throws Exception {
        // given
        when(configProperties.getBaseUrl()).thenReturn("http://someBaseUrl");
        when(configProperties.getSecretPath()).thenReturn("/someSecretPath");

        testee = vaultClient(configProperties, "someClientToken");
        testee.asyncHttpClient = asyncHttpClient;

        // when
        testee.readFields("someKey");

        // then
        verify(asyncHttpClient).prepareGet("http://someBaseUrl/v1/someSecretPath/someKey");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldAddMissingUrlSlashes() throws Exception {
        // given
        when(configProperties.getBaseUrl()).thenReturn("http://someBaseUrl");
        when(configProperties.getSecretPath()).thenReturn("/someSecretPath");

        testee = vaultClient(configProperties, "someClientToken");
        testee.asyncHttpClient = asyncHttpClient;

        // when
        testee.readFields("someKey");

        // then
        verify(asyncHttpClient).prepareGet("http://someBaseUrl/v1/someSecretPath/someKey");
    }
}
