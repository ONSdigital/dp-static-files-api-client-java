package com.github.onsdigital.dp.files.api;

public interface Client {

    /**
     * Publishes Files held in the Static Files Service that are associated with the provided collectionId.
     *
     * @param collectionId
     */
    void publishCollection(String collectionId);
}
