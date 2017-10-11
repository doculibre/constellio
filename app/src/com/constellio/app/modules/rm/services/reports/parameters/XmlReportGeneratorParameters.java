package com.constellio.app.modules.rm.services.reports.parameters;

import com.constellio.app.modules.rm.services.reports.XmlReportGenerator;
import com.constellio.model.entities.records.Record;

import java.util.List;

public class XmlReportGeneratorParameters extends XmlGeneratorParameters {
    private int numberOfCopies;

    public XmlReportGeneratorParameters() { }

    public XmlReportGeneratorParameters(int numberOfCopies, Record... recordsElements) {
        super(recordsElements);
        this.numberOfCopies = numberOfCopies;
    }

    public XmlReportGeneratorParameters setNumberOfCopies(int numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
        return this;
    }

    public int getNumberOfCopies(){
        return this.numberOfCopies;
    }

    @Override
    public void validateInputs() {

    }
}
