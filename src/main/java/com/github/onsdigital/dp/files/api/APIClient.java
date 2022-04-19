package com.github.onsdigital.dp.files.api;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class APIClient implements Client {
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
        CloseableHttpResponse httpResponse;
        HttpPatch request = new HttpPatch(hostname + "/collection/" + collectionId);
        request.addHeader("Authorization", "Bearer " + authToken);

        try {
            httpResponse = httpClient.execute(request);
        } catch (Exception e) {
            throw new ConnectionException("error talking to files api", e);
        }

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_CREATED) {
            return;
        }

        switch (statusCode) {
            case HttpStatus.SC_NOT_FOUND ->
                    throw new NoFilesInCollectionException("No files found in collection: " + collectionId);
            case HttpStatus.SC_CONFLICT ->
                    throw new FileInvalidStateException("file in collection: " + collectionId + " not in a publishable state");
            case HttpStatus.SC_FORBIDDEN ->
                    throw new UnauthorizedException("You are not authorized to publish collections");
            case HttpStatus.SC_INTERNAL_SERVER_ERROR -> {
                String body = EntityUtils.toString(httpResponse.getEntity());
                throw new ServerErrorException("Server error returned from file api: " + body);
            }
            default -> {
                String body = EntityUtils.toString(httpResponse.getEntity());
                throw new UnexpectedResponseException("Unexpected error from file api: " + body);
            }
        }
    }
}
