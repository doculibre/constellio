package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.List;

public class ReportVO implements Serializable{
    String title;
    String user;
    String schemaTypeCode;
    List<ReportedMetadataVO> reportedMetadataVOList;

    public String getTitle() {
        return title;
    }

    public ReportVO setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getUser() {
        return user;
    }

    public ReportVO setUser(String user) {
        this.user = user;
        return this;
    }

    public String getSchemaTypeCode() {
        return schemaTypeCode;
    }

    public ReportVO setSchemaTypeCode(String schemaTypeCode) {
        this.schemaTypeCode = schemaTypeCode;
        return this;
    }

    public List<ReportedMetadataVO> getReportedMetadataVOList() {
        return reportedMetadataVOList;
    }

    public ReportVO setReportedMetadataVOList(List<ReportedMetadataVO> reportedMetadataVOList) {
        this.reportedMetadataVOList = reportedMetadataVOList;
        return this;
    }
}
