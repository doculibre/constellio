package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;

import static com.constellio.app.ui.i18n.i18n.$;

public enum PrintableReportListPossibleType {
    FOLDER(Folder.SCHEMA_TYPE), DOCUMENT(Document.SCHEMA_TYPE), TASK(Task.SCHEMA_TYPE), CONTAINER(ContainerRecord.SCHEMA_TYPE), ANY(null);

    private final String schemaType;

    PrintableReportListPossibleType(String schemaType) {
        this.schemaType = schemaType;
    }

    public String getLabel() {
        return $("PrintableReportListPossibleType." + schemaType);
    }

    public static PrintableReportListPossibleType getValue(String value) {
            for(PrintableReportListPossibleType e: PrintableReportListPossibleType.values()) {
                if(e.name().equals(value)) {
                    return e;
                }
            }
            return null;// not found
    }

    public String getSchemaType() {
        return this.schemaType;
    }


}
