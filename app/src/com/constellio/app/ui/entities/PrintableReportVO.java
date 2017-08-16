package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.List;

public class PrintableReportVO extends RecordVO implements Serializable {
    private String title;
    private String jasperFile;
    private String reportType;
    private boolean deletable;
    private String recordSchema;

    public PrintableReportVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
        super(id, metadataValues, viewMode);
    }

    public String getTitle() {
        return title;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public String getJasperFile() {
        return jasperFile;
    }

    public String getReportType() {
        return reportType;
    }

    public String getRecordSchema() {
        return recordSchema;
    }

    public PrintableReportVO setJasperFile(String jasperFile) {
        this.jasperFile = jasperFile;
        return this;
    }

    public PrintableReportVO setReportType(String reportType) {
        this.reportType = reportType;
        return this;
    }

    public PrintableReportVO setRecordSchema(String recordSchema) {
        this.recordSchema = recordSchema;
        return this;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }
}
