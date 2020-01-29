package com.constellio.model.entities.schemas;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public class InheritedMetadataBehaviors implements Serializable {

	final boolean undeletable;

	final boolean multivalue;
	final boolean systemReserved;
	final boolean unmodifiable;
	final boolean uniqueValue;
	final boolean childOfRelationship;
	final boolean taxonomyRelationship;
	final boolean sortable;
	final boolean searchable;
	final boolean schemaAutocomplete;
	final boolean essential;
	final boolean encrypted;
	final boolean essentialInSummary;
	final boolean availableInSummary;
	final boolean multiLingual;
	final boolean markedForDeletion;
	final boolean reverseDependency;
	final boolean relationshipProvidingSecurity;
	final boolean dependencyOfAutomaticMetadata;
	final boolean cacheIndex;

	final Integer maxLength;

	final MetadataTransiency volatility;
	final Set<String> customAttributes;

	public InheritedMetadataBehaviors(boolean undeletable, boolean multivalue, boolean systemReserved,
									  boolean unmodifiable,
									  boolean uniqueValue, boolean childOfRelationship, boolean taxonomyRelationship,
									  boolean sortable,
									  boolean searchable, boolean schemaAutocomplete, boolean essential,
									  boolean encrypted, boolean essentialInSummary, boolean availableInSummary,
									  boolean multiLingual, boolean markedForDeletion, Set<String> customAttributes,
									  boolean reverseDependency,
									  boolean relationshipProvidingSecurity, MetadataTransiency volatility,
									  boolean dependencyOfAutomaticMetadata, boolean cacheIndex, Integer maxLength) {
		this.undeletable = undeletable;
		this.multivalue = multivalue;
		this.systemReserved = systemReserved;
		this.unmodifiable = unmodifiable;
		this.uniqueValue = uniqueValue;
		this.childOfRelationship = childOfRelationship;
		this.taxonomyRelationship = taxonomyRelationship;
		this.sortable = sortable;
		this.searchable = searchable;
		this.schemaAutocomplete = schemaAutocomplete;
		this.essential = essential;
		this.encrypted = encrypted;
		this.essentialInSummary = essentialInSummary;
		this.availableInSummary = availableInSummary;
		this.multiLingual = multiLingual;
		this.markedForDeletion = markedForDeletion;
		this.customAttributes = Collections.unmodifiableSet(customAttributes);
		this.reverseDependency = reverseDependency;
		this.relationshipProvidingSecurity = relationshipProvidingSecurity;
		this.volatility = volatility == null ? MetadataTransiency.PERSISTED : volatility;
		this.dependencyOfAutomaticMetadata = dependencyOfAutomaticMetadata;
		this.cacheIndex = cacheIndex;
		this.maxLength = maxLength;
	}

	public boolean isReverseDependency() {
		return reverseDependency;
	}

	public boolean isUndeletable() {
		return undeletable;
	}

	public boolean isMultivalue() {
		return multivalue;
	}

	public boolean isEssentialInSummary() {
		return essentialInSummary;
	}

	public boolean isAvailableInSummary() {
		return availableInSummary;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public boolean isSystemReserved() {
		return systemReserved;
	}

	public boolean isUnmodifiable() {
		return unmodifiable;
	}

	public boolean isUniqueValue() {
		return uniqueValue;
	}

	public boolean isChildOfRelationship() {
		return childOfRelationship;
	}

	public boolean isTaxonomyRelationship() {
		return taxonomyRelationship;
	}

	public boolean isSortable() {
		return sortable;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public MetadataTransiency getTransiency() {
		return volatility;
	}

	public boolean isSchemaAutocomplete() {
		return schemaAutocomplete;
	}

	public boolean isEssential() {
		return essential;
	}

	public boolean isCacheIndex() {
		return cacheIndex || childOfRelationship || taxonomyRelationship;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public boolean isMultiLingual() {
		return multiLingual;
	}

	public boolean isMarkedForDeletion() {
		return markedForDeletion;
	}

	public boolean isRelationshipProvidingSecurity() {
		return relationshipProvidingSecurity;
	}

	public boolean isDependencyOfAutomaticMetadata() {
		return dependencyOfAutomaticMetadata;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public Set<String> getCustomAttributes() {
		return customAttributes;
	}

}
