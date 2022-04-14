package com.github.onsdigital.dp.files.api;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class APIClient implements Client{
    private String hostname;
    private String authToken;
    private CloseableHttpClient httpClient;
    public APIClient(String hostname, String authToken) {

        this.hostname = hostname;
        this.authToken = authToken;
        httpClient = HttpClients.createDefault();
    }

    @Override
    public void publishCollection(String collectionId) throws Exception {
        try {
            HttpPatch request = new HttpPatch(hostname + "/collection/" + collectionId);
            request.addHeader("Authorization", "Bearer " + authToken);
            CloseableHttpResponse httpResponse = httpClient.execute(request);

        } catch (Exception e) {
            throw e;
        }
    }
}
