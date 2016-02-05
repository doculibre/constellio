package com.constellio.app.ui.pages.search.criteria;

import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.ModifiableStructure;

public class FacetSelections implements ModifiableStructure {

	private String facetField;

	private Set<String> selectedValues;

	boolean dirty;

	public FacetSelections() {

	}

	public FacetSelections(String facetField, Set<String> selectedValues) {
		this.facetField = facetField;
		this.selectedValues = selectedValues;
		this.dirty = false;
	}

	public String getFacetField() {
		return facetField;
	}

	public void setFacetField(String facetField) {
		this.dirty = true;
		this.facetField = facetField;
	}

	public Set<String> getSelectedValues() {
		return selectedValues;
	}

	public void setSelectedValues(Set<String> selectedValues) {
		this.dirty = true;
		this.selectedValues = selectedValues;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "SelectedFacets{" +
				"facetField='" + facetField + '\'' +
				", selectedValues=" + selectedValues +
				", dirty=" + dirty +
				'}';
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}

}
