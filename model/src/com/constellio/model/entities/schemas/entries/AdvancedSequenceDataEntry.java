package com.constellio.model.entities.schemas.entries;

public class AdvancedSequenceDataEntry implements DataEntry {

	final AdvancedSequenceCalculator calculator;

	public AdvancedSequenceDataEntry(AdvancedSequenceCalculator calculator) {
		super();
		this.calculator = calculator;
	}

	public AdvancedSequenceCalculator getCalculator() {
		return calculator;
	}

	public DataEntryType getType() {
		return DataEntryType.ADVANCED_SEQUENCE;
	}

}
