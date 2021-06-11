package com.constellio.app.modules.rm.services.reports.xml.legacy.parameters;

import com.constellio.model.entities.records.Record;

// Use XMLDataSourceGeneratorFactory instead
@Deprecated
public class XmlReportGeneratorParameters extends AbstractXmlGeneratorParameters {
	private int numberOfCopies;

	public XmlReportGeneratorParameters() {
	}

	public XmlReportGeneratorParameters(int numberOfCopies, Record... recordsElements) {
		super(recordsElements);
		this.numberOfCopies = numberOfCopies;
	}

	/**
	 * Method used to set the number of copies needed of the report.
	 *
	 * @param numberOfCopies
	 * @return
	 */
	public XmlReportGeneratorParameters setNumberOfCopies(int numberOfCopies) {
		this.numberOfCopies = numberOfCopies;
		return this;
	}

	public int getNumberOfCopies() {
		return this.numberOfCopies;
	}

	@Override
	public void validateInputs() {

	}
}
