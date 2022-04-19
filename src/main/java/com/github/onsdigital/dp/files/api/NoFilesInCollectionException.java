package com.github.onsdigital.dp.files.api;

public class NoFilesInCollectionException extends RuntimeException {

    public NoFilesInCollectionException(String errorMessage) {
        super(errorMessage);
    }
}
