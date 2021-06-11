package com.constellio.model.entities.schemas;

import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class QueryBasedReindexingBatchProcessModificationImpact implements ModificationImpact {

	final MetadataSchemaType impactedSchemaType;
	final List<Metadata> metadataToReindex;
	final LogicalSearchCondition logicalSearchCondition;
	final int potentialImpactsCount;
	final String transactionTitle;
	final boolean handledNow;


	@Override
	public String getCollection() {
		return impactedSchemaType.getCollection();
	}

	public List<Metadata> getMetadataToReindex() {
		return metadataToReindex;
	}

	@Override
	public List<ModificationImpactDetail> getDetails() {
		return Collections.singletonList(new ModificationImpactDetail(impactedSchemaType, potentialImpactsCount));
	}

	@Override
	public boolean isHandledNow() {
		return handledNow;
	}

	public int getPotentialImpactsCount() {
		return potentialImpactsCount;
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


	public String getTransactionTitle() {
		return transactionTitle;
	}

	@Override
	public String toString() {
		return "QueryBasedReindexingBatchProcessModificationImpact{" +
			   "impactedSchemaType=" + impactedSchemaType +
			   ", metadataToReindex=" + metadataToReindex +
			   ", logicalSearchCondition=" + logicalSearchCondition +
			   ", potentialImpactsCount=" + potentialImpactsCount +
			   ", handledNow=" + handledNow +
			   '}';
	}
}
