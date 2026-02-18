package com.github.onsdigital.dp.files.api;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
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

        CloseableHttpResponse resp;
        try {
            HttpPatch request = new HttpPatch(removeTrailingSlash(hostname) + "/collection/" + collectionId);
            resp = executeRequest(request);
        } catch (IllegalArgumentException | IOException e) {
            //TODO: Invalid hostname isn't getting caught at build time but
            // at request time. This should be reworked.
            throw new ConnectionException("error connecting to files api", e);
        }

        try (CloseableHttpResponse response = resp) {
            int statusCode = response.getCode();
            if (statusCode == HttpStatus.SC_CREATED) {
                return;
            }

            String body = getErrorStringFromResponse(response.getEntity());

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
        } catch (IOException e) {
            throw new ConnectionException("error reading response from files api", e);
        }
    }

    private String getErrorStringFromResponse(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity);
        } catch (IOException | ParseException e) {
            return "ERROR GETTING BODY FROM RESPONSE OBJECT";
        }
    }

    private CloseableHttpResponse executeRequest(HttpUriRequest request) throws IOException {
        request.addHeader("Authorization", "Bearer " + authToken);
        // TODO: remove reliance on CloseableHttpClient.execute
        // as it is deprecated in HttpClient 5.4.0 - instead
        // use HttpClient.execute with a ResponseHandler
        return httpClient.execute(request);
    }

    private String removeTrailingSlash(String url){
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url; // Return unchanged string if the string does not end with a slash
    }
}
