package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import org.joda.time.LocalDateTime;

import java.util.List;

public class ImportExportAudit extends RecordWrapper {
    public static final String SCHEMA_TYPE = "importExportAudit";
    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
    public static final String START_DATE = "startingDate";
    public static final String END_DATE = "endDate";
    public static final String ERRORS = "errors";
    public static final String TYPE = "type";
    public static final String CONTENT = "content";

    public enum ExportImport implements EnumWithSmallCode {
        EXPORT("E"),
        IMPORT("I");

        private String code;

        ExportImport(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public ImportExportAudit(Record record,
                             MetadataSchemaTypes types) {
        super(record, types, SCHEMA_TYPE);
    }

    public LocalDateTime getStartDate() {
        return get(START_DATE);
    }

    public LocalDateTime getEndDate() {
        return get(END_DATE);
    }

    public ImportExportAudit setEndDate(LocalDateTime localDateTime) {
        set(END_DATE, localDateTime);
        return this;
    }

    public ImportExportAudit setStartDate(LocalDateTime localDateTime) {
        set(START_DATE, localDateTime);
        return this;
    }


    public ImportExportAudit LocalDateTime(String localDate) {
        set(END_DATE, localDate);
        return this;
    }

    public List<String> getErrors() {
        return get(ERRORS);
    }

    public ImportExportAudit setErrors(List<String> stringList) {
        set(ERRORS, stringList);
        return this;
    }

    public ImportExportAudit setType(ExportImport exportImport) {
        set(TYPE, exportImport);
        return this;
    }

    public ExportImport getType() {
        return get(TYPE);
    }

    public ImportExportAudit setContent(Content content) {
        set(CONTENT, content);
        return this;
    }

    public Content getContent() {
        return get(CONTENT);
    }
}
