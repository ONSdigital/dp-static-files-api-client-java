package com.github.onsdigital.dp.files.api;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class APIClientTest {
    public static final String COLLECTION_ID = "collection-id";
    public static final String TOKEN = "AUTHENTICATION-TOKEN";

    @Test
    void successfullyPublishingCollection() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.SC_CREATED));

        HttpUrl url = server.url("");

        Client client = new APIClient(url.toString(), TOKEN);

        try {
            client.publishCollection(COLLECTION_ID);
        } catch (Exception e) {
            Assertions.fail("No Exception should have been thrown");
        }

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("PATCH", request.getMethod());
        assertEquals("/collection/" + COLLECTION_ID, request.getPath());
        assertEquals("Bearer " + TOKEN, request.getHeader("Authorization"));
    }

    @Test
    void attemptingToPublishACollectionWithNoFiles() {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.SC_NOT_FOUND));

        HttpUrl url = server.url("");

        APIClient client = new APIClient(url.toString(), TOKEN);

        assertThrows(NoFilesInCollectionException.class, () -> {
            client.publishCollection(COLLECTION_ID);
        });
    }

    @Test
    void attemptingToPublishACollectionWithAFileThatIsNotInUploadedState() {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.SC_CONFLICT));

        HttpUrl url = server.url("");

        APIClient client = new APIClient(url.toString(), TOKEN);

        assertThrows(FileInvalidStateException.class, () -> {
            client.publishCollection(COLLECTION_ID);
        });
    }

    @Test
    void handingAuthorizationFailure() {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.SC_FORBIDDEN));

        HttpUrl url = server.url("");

        APIClient client = new APIClient(url.toString(), TOKEN);

        assertThrows(UnauthorizedException.class, () -> {
            client.publishCollection(COLLECTION_ID);
        });
    }

    @Test
    void handlingServerFailure() {
        String errBody = "the files server is broken";
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.SC_INTERNAL_SERVER_ERROR).setBody(new Buffer().writeUtf8(errBody)));

        HttpUrl url = server.url("");

        APIClient client = new APIClient(url.toString(), TOKEN);

        Exception e = assertThrows(ServerErrorException.class, () -> {
            client.publishCollection(COLLECTION_ID);
        });

        assertTrue(e.getMessage().contains(errBody));
    }

    @Test
    void handlingUnexpectedError() {
        String responseBody = "its always tea time";
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(418).setBody(new Buffer().writeUtf8(responseBody)));

        HttpUrl url = server.url("");

        APIClient client = new APIClient(url.toString(), TOKEN);

        Exception e = assertThrows(UnexpectedResponseException.class, () -> {
            client.publishCollection(COLLECTION_ID);
        });

        assertTrue(e.getMessage().contains(responseBody));
    }

    @Test
    void handingInvalidHostnameProvided() {
        APIClient client = new APIClient("NOT A VALID HOSTNAME", TOKEN);

        Exception e = assertThrows(ConnectionException.class, () -> {
            client.publishCollection(COLLECTION_ID);
        });

        assertNotNull(e.getCause());
    }

    @Test
    void handingIncorrectHostnameProvided() {
        APIClient client = new APIClient("http://localhost:123456789", TOKEN);

        Exception e = assertThrows(ConnectionException.class, () -> {
            client.publishCollection(COLLECTION_ID);
        });

        assertNotNull(e.getCause());
    }
}