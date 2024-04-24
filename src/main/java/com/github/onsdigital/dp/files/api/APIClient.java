package com.github.onsdigital.dp.files.api;


import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;

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
    public void publishCollection(String collectionId){
        CloseableHttpResponse httpResponse;

        try {
            HttpPatch request = new HttpPatch(hostname + "/collection/" + collectionId);
            request.addHeader("Authorization", "Bearer " + authToken);
            httpResponse = httpClient.execute(request);
        } catch (Exception e) {
            throw new ConnectionException("error talking to files api", e);
        }

        int statusCode = httpResponse.getCode();
        if (statusCode == HttpStatus.SC_CREATED) {
            return;
        }

        String body;
        try {
            body = EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            body = "ERROR GETTING BODY FROM RESPONSE OBJECT";
        }

        switch (statusCode) {
            case HttpStatus.SC_NOT_FOUND:
                throw new NoFilesInCollectionException("No files found in collection: " + collectionId);
            case HttpStatus.SC_CONFLICT:
                throw new FileInvalidStateException("file in collection: " + collectionId + " not in a publishable state");
            case HttpStatus.SC_FORBIDDEN:
                throw new UnauthorizedException("You are not authorized to publish collections");
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                throw new ServerErrorException("Server error returned from file api: " + body);
            default:
                throw new UnexpectedResponseException("Unexpected error from file api: " + body);
        }
    }
}
