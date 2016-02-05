package com.constellio.model.entities.schemas;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ModificationImpact {

	final List<Metadata> metadataToReindex;
	final LogicalSearchCondition logicalSearchCondition;

	public ModificationImpact(List<Metadata> metadataToReindex,
			LogicalSearchCondition logicalSearchCondition) {
		this.metadataToReindex = metadataToReindex;
		this.logicalSearchCondition = logicalSearchCondition;

		if (logicalSearchCondition == null) {
			throw new RuntimeException("logicalSearchCondition required");
		}
	}

	public List<Metadata> getMetadataToReindex() {
		return metadataToReindex;
	}

	public LogicalSearchCondition getLogicalSearchCondition() {
		return logicalSearchCondition;
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
		return "ModificationImpact{" +
				"metadataToReindex=" + metadataToReindex +
				", logicalSearchCondition=" + logicalSearchCondition +
				'}';
	}
}
