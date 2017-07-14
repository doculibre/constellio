package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.vaadin.data.util.converter.Converter;

import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Marco on 2017-07-07.
 */
public enum PrintableReportListPossibleView {
    FOLDER(Folder.SCHEMA_TYPE), DOCUMENT(Document.SCHEMA_TYPE), TASK(Task.SCHEMA_TYPE), CONTAINER(ContainerRecord.SCHEMA_TYPE);

    private final String value;

    PrintableReportListPossibleView(String value) {
        this.value = value;
    }

    public String getLabel() {
        return $("PrintableReportListPossibleView." + value);
    }

    public static PrintableReportListPossibleView getValue(String value) {
            for(PrintableReportListPossibleView e: PrintableReportListPossibleView.values()) {
                if(e.value.equals(value)) {
                    return e;
                }
            }
            return null;// not found
    }

    @Override
    public String toString() {
        return this.value;
    }


}
