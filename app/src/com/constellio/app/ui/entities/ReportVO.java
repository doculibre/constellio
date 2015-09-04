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
