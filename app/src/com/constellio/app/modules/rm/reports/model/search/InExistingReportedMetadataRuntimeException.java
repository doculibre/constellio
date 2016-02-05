package com.constellio.app.modules.rm.reports.model.search;

import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;

public class InExistingReportedMetadataRuntimeException extends RuntimeException {
    public InExistingReportedMetadataRuntimeException(ReportedMetadata reportedMetadata, String schemaTypeCode) {
        super(reportedMetadata.getMetadataCode() + ", " + schemaTypeCode);
    }
}
