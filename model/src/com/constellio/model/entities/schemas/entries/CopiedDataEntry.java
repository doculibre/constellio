package com.constellio.model.entities.schemas.entries;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CopiedDataEntry implements DataEntry {

	final String referenceMetadata;

	final String copiedMetadata;

	public CopiedDataEntry(String referenceMetadata, String copiedMetadata) {
		super();
		this.referenceMetadata = referenceMetadata;
		this.copiedMetadata = copiedMetadata;
	}

	public String getReferenceMetadata() {
		return referenceMetadata;
	}

	public String getCopiedMetadata() {
		return copiedMetadata;
	}

	@Override
	public DataEntryType getType() {
		return DataEntryType.COPIED;
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
		return "CopiedDataEntry [referenceMetadata=" + referenceMetadata + ", copiedMetadata=" + copiedMetadata + "]";
	}
}
