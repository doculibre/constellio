package com.constellio.app.ui.framework.builders;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.ReportVO;
import com.constellio.app.ui.entities.ReportedMetadataVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReportToVOBuilder implements Serializable{
    public ReportVO build(Report report) {
        List<ReportedMetadataVO> reportedMetadataList = new ArrayList<>();
        for(ReportedMetadata reportedMetadata : report.getReportedMetadata()){
            String code = reportedMetadata.getMetadataCode();
            int x = reportedMetadata.getXPosition();
            int y = reportedMetadata.getYPosition();
            ReportedMetadataVO reportedMetadataVO = new ReportedMetadataVO().setMetadataCode(code).setXPosition(x).setYPosition(y);
            reportedMetadataList.add(reportedMetadataVO);
        }
        String title = report.getTitle();
        String user = report.getUsername();
        String schemaTypeCode = report.getSchemaTypeCode();
        return new ReportVO().setTitle(title).setUser(user).setSchemaTypeCode(schemaTypeCode).setReportedMetadataVOList(reportedMetadataList);
    }
}
