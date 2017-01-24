package com.constellio.model.entities.schemas;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class InheritedMetadataBehaviors {

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
	final boolean multiLingual;
	final boolean markedForDeletion;
	final boolean reverseDependency;
	final Set<String> customAttributes;

	public InheritedMetadataBehaviors(boolean undeletable, boolean multivalue, boolean systemReserved, boolean unmodifiable,
			boolean uniqueValue, boolean childOfRelationship, boolean taxonomyRelationship, boolean sortable,
			boolean searchable, boolean schemaAutocomplete, boolean essential, boolean encrypted, boolean essentialInSummary,
			boolean multiLingual, boolean markedForDeletion, Set<String> customAttributes, boolean reverseDependency) {
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
		this.multiLingual = multiLingual;
		this.markedForDeletion = markedForDeletion;
		this.customAttributes = Collections.unmodifiableSet(customAttributes);
		this.reverseDependency = reverseDependency;
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

	public boolean isSchemaAutocomplete() {
		return schemaAutocomplete;
	}

	public boolean isEssential() {
		return essential;
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

	public Set<String> getCustomAttributes() {
		return customAttributes;
	}
}
