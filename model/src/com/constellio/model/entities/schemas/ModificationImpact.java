package com.constellio.model.entities.schemas;

import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.Set;

public class ModificationImpact {

	final MetadataSchemaType impactedSchemaType;
	final List<Metadata> metadataToReindex;
	final LogicalSearchCondition logicalSearchCondition;
	final Set<String> markForReindexingInsteadOfBatchProcess;
	final int potentialImpactsCount;
	final String transactionTitle;

	public ModificationImpact(MetadataSchemaType impactedSchemaType, List<Metadata> metadataToReindex,
							  LogicalSearchCondition logicalSearchCondition, int potentialImpactsCount,
							  String transactionTitle) {
		this.impactedSchemaType = impactedSchemaType;
		this.metadataToReindex = metadataToReindex;
		this.logicalSearchCondition = logicalSearchCondition;
		this.markForReindexingInsteadOfBatchProcess = null;
		this.potentialImpactsCount = potentialImpactsCount;
		this.transactionTitle = transactionTitle;

		if (logicalSearchCondition == null) {
			throw new RuntimeException("logicalSearchCondition required");
		}
	}

	public ModificationImpact(MetadataSchemaType impactedSchemaType, List<Metadata> metadataToReindex,
							  LogicalSearchCondition logicalSearchCondition,
							  Set<String> markForReindexingInsteadOfBatchProcess, int potentialImpactsCount,
							  String transactionTitle) {
		this.impactedSchemaType = impactedSchemaType;
		this.metadataToReindex = metadataToReindex;
		this.logicalSearchCondition = logicalSearchCondition;
		this.markForReindexingInsteadOfBatchProcess = markForReindexingInsteadOfBatchProcess;
		this.potentialImpactsCount = potentialImpactsCount;
		this.transactionTitle = transactionTitle;

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

	public MetadataSchemaType getImpactedSchemaType() {
		return impactedSchemaType;
	}

	public int getPotentialImpactsCount() {
		return potentialImpactsCount;
	}

	public String getTransactionTitle() {
		return transactionTitle;
	}

	public Set<String> getMarkForReindexingInsteadOfBatchProcess() {
		return markForReindexingInsteadOfBatchProcess;
	}

	@Override
	public String toString() {
		return "ModificationImpact{" +
			   "impactedSchemaType=" + impactedSchemaType +
			   ", metadataToReindex=" + metadataToReindex +
			   ", logicalSearchCondition=" + logicalSearchCondition +
			   ", potentialImpactsCount=" + potentialImpactsCount +
			   '}';
	}
}
