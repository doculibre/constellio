package com.constellio.model.services.records;

public class RecordServicesWrapperRuntimeException extends RuntimeException {
    private RecordServicesException wrappedException;

    public RecordServicesWrapperRuntimeException(RecordServicesException exceptionToWrap) {
        super(exceptionToWrap);
        this.wrappedException = exceptionToWrap;
    }

    public RecordServicesException getWrappedException() {
        return wrappedException;
    }
}