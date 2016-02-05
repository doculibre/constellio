package com.constellio.app.services.schemas.bulkImport.data.xml;

public class InvalidIdRuntimeException extends RuntimeException {
    public InvalidIdRuntimeException(String id) {
        super("Invalid id " + id);
    }
}
