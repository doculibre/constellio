package com.constellio.app.modules.rm.services.reports.parameters;

import com.constellio.model.entities.records.Record;

/**
 * Created by Marco on 2017-07-05.
 */
public abstract class XmlGeneratorParameters {

    public Record[] recordsElements;

    public XmlGeneratorParameters(Record... recordsElements) {
        this.setRecordsElements(recordsElements);
    }

    public XmlGeneratorParameters setRecordsElements(Record... recordsElements) {
        this.recordsElements = recordsElements;
        return this;
    }

    public Record[] getRecordsElements() {
        return this.recordsElements;
    }

    public abstract void validateInputs();
}
