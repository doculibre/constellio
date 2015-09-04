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
package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

public class Report extends RecordWrapper{
    public static final String SCHEMA_TYPE = "report";
    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
    public static final String USERNAME = "username";
    public static final String SEPARATOR = "separator";
    public static final String SCHEMA_TYPE_CODE = "schemaTypeCode";
    public static final String REPORTED_METADATA = "reportedMetadata";
    public static final String COLUMNS_COUNT = "columnsCount";
    public static final String LINES_COUNT = "linesCount";

    public Report(Record record, MetadataSchemaTypes types) {
        super(record, types, SCHEMA_TYPE + "_");
    }

    public Report setTitle(String title) {
        super.setTitle(title);
        return this;
    }

    public String getUsername() {
        return get(USERNAME);
    }

    public Report setUsername(String username) {
        set(USERNAME, username);
        return this;
    }

    public String getSchemaTypeCode() {
        return get(SCHEMA_TYPE_CODE);
    }

    public Report setSchemaTypeCode(String schemaTypeCode) {
        set(SCHEMA_TYPE_CODE, schemaTypeCode);
        return this;
    }

    public String getSeparator() {
        return get(SEPARATOR);
    }

    public Report setSeparator(String separator) {
        set(SEPARATOR, separator);
        return this;
    }

    public int getColumnsCount() {
        return ((Double)get(COLUMNS_COUNT)).intValue();
    }

    public Report setColumnsCount(int columnsCount) {
        set(COLUMNS_COUNT, columnsCount);
        return this;
    }

    public int getLinesCount() {
        Double linesCount = get(LINES_COUNT);
        return linesCount.intValue();
    }

    public Report setLinesCount(int columnsCount) {
        set(LINES_COUNT, columnsCount);
        return this;
    }

    public List<ReportedMetadata> getReportedMetadata() {
        return getList(REPORTED_METADATA);
    }

    public Report setReportedMetadata(List<ReportedMetadata> documentTypesDetails) {
        set(REPORTED_METADATA, documentTypesDetails);
        return this;
    }

}
