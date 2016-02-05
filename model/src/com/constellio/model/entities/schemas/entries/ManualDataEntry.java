package com.constellio.model.entities.schemas.entries;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ManualDataEntry implements DataEntry {

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public DataEntryType getType() {
		return DataEntryType.MANUAL;
	}

	@Override
	public String toString() {
		return "ManualDataEntry []";
	}

}
