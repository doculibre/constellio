package com.constellio.app.modules.rm.services.reports.parameters;

import com.constellio.model.entities.records.Record;
import com.google.common.base.Strings;

import java.util.List;

public abstract class AbstractXmlGeneratorParameters {

    private Record[] recordsElements;

    private List<String> ids;
    private String schemaCode;

    private boolean isForTest = false;

    public AbstractXmlGeneratorParameters(Record... recordsElements) {
        this.setRecordsElements(recordsElements);
    }

    public AbstractXmlGeneratorParameters setRecordsElements(Record... recordsElements) {
        this.recordsElements = recordsElements;
        return this;
    }


    public void setElementWithIds(String schemaCode, List<String> ids) {
        this.schemaCode = schemaCode;
        this.ids = ids;
    }

    public List<String> getIdsOfElement() {
        return this.ids;
    }

    public String getSchemaCode(){
        return this.schemaCode;
    }

    public Record[] getRecordsElements() {
        return this.recordsElements;
    }

    public boolean isParametersUsingIds() {
        return recordsElements.length == 0 && ids.size() > 0 && !Strings.isNullOrEmpty(this.schemaCode);
    }

    public AbstractXmlGeneratorParameters markAsTestXml() {
        isForTest = true;
        return this;
    }

    public AbstractXmlGeneratorParameters markNotAsTestXml() {
        isForTest = false;
        return this;
    }

    public boolean isForTest() {
        return isForTest;
    }

    public abstract void validateInputs();
}
