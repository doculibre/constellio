/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
