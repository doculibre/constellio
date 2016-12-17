package com.constellio.model.entities.schemas.entries;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SequenceDataEntry implements DataEntry {

	final String fixedSequenceCode;

	final String metadataProvidingSequenceCode;

	public SequenceDataEntry(String fixedSequenceCode, String metadataProvidingSequenceCode) {
		super();
		this.fixedSequenceCode = fixedSequenceCode;
		this.metadataProvidingSequenceCode = metadataProvidingSequenceCode;
	}

	public String getFixedSequenceCode() {
		return fixedSequenceCode;
	}

	public String getMetadataProvidingSequenceCode() {
		return metadataProvidingSequenceCode;
	}

	@Override
	public DataEntryType getType() {
		return DataEntryType.SEQUENCE;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return "SequenceDataEntry [fixedSequenceCode=" + fixedSequenceCode + ", metadataProvidingSequenceCode=" +
				metadataProvidingSequenceCode + "]";
	}

}
