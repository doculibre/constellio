package com.constellio.model.entities.schemas.entries;

public class AgregatedDataEntry implements DataEntry {

	private String inputMetadata;

	private String referenceMetadata;

	private AgregationType agregationType;

	public AgregatedDataEntry(String inputMetadata, String referenceMetadata, AgregationType agregationType) {
		this.inputMetadata = inputMetadata;
		this.referenceMetadata = referenceMetadata;
		this.agregationType = agregationType;
	}

	public String getInputMetadata() {
		return inputMetadata;
	}

	public String getReferenceMetadata() {
		return referenceMetadata;
	}

	public AgregationType getAgregationType() {
		return agregationType;
	}

	@Override
	public DataEntryType getType() {
		return DataEntryType.AGREGATED;
	}
}
