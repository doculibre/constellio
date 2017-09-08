package com.constellio.app.modules.rm.services.reports.parameters;

import com.constellio.model.entities.records.Record;

import java.util.List;

public class XmlReportGeneratorParameters extends XmlGeneratorParameters {
    private int numberOfCopies;

    public XmlReportGeneratorParameters(int numberOfCopies, Record... recordsElements) {
        super(recordsElements);
        this.numberOfCopies = numberOfCopies;
    }

    public int getNumberOfCopies(){
        return this.numberOfCopies;
    }

    @Override
    public void validateInputs() {

    }
}
