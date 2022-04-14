package com.github.onsdigital.dp.files.api;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class APIClientTest {
    public static final String COLLECTION_ID = "collection-id";
    public static final String TOKEN = "AUTHENTICATION-TOKEN";

    @Test
    void successfullyPublishingCollection() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.SC_CREATED));

        HttpUrl url = server.url("");

        APIClient client = new APIClient(url.toString(), TOKEN);

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
}