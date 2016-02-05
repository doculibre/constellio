package com.constellio.app.modules.rm.reports.model.search;

public class NoSuchReportRuntimeException extends RuntimeException {
    public NoSuchReportRuntimeException(String username, String schemaType, String reportTitle) {
        super(username + ", " + schemaType + ", " + reportTitle);
    }
}
