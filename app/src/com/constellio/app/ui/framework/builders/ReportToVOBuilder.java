/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
