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
package com.constellio.app.ui.entities;

import java.io.Serializable;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.SchemaUtils;

@SuppressWarnings("serial")
public class FormMetadataVO implements Serializable {
	String code;
	String localcode;
	MetadataValueType valueType;
	MetadataSchemaVO schema;
	String reference;
	String label;
	boolean required;
	boolean multivalue;
	boolean searchable;
	boolean sortable;
	boolean advancedSearch;
	boolean facet;
	boolean highlight;
	boolean autocomplete;
	boolean enabled;
	String metadataGroup;
	MetadataInputType input;
	Object defaultValue;

	public FormMetadataVO(String code, MetadataValueType type, boolean required, MetadataSchemaVO schemaVO, String reference,
			String label, boolean searchable, boolean multivalue, boolean sortable, boolean advancedSearch, boolean facet,
			MetadataInputType input, boolean highlight, boolean autocomplete, boolean enabled, String metadataGroup, Object defaultValue) {
		super();

		String localCodeParsed = SchemaUtils.underscoreSplitWithCache(code)[2];
		if (localCodeParsed.contains("USR")) {
			localCodeParsed = localCodeParsed.split("USR")[1];
		}

		this.code = code;
		this.localcode = localCodeParsed;
		this.valueType = type;
		this.required = required;
		this.schema = schemaVO;
		this.label = label;
		this.multivalue = multivalue;
		this.searchable = searchable;
		this.sortable = sortable;
		this.advancedSearch = advancedSearch;
		this.facet = facet;
		this.reference = reference;
		this.input = input;
		this.highlight = highlight;
		this.autocomplete = autocomplete;
		this.enabled = enabled;
		this.metadataGroup = metadataGroup;
		this.defaultValue = defaultValue;
	}

	public FormMetadataVO() {
		super();
		this.code = "";
		this.localcode = "";
		this.valueType = null;
		this.required = false;
		this.schema = null;
		this.label = "";
		this.multivalue = false;
		this.searchable = false;
		this.sortable = false;
		this.advancedSearch = false;
		this.facet = false;
		this.reference = null;
		this.input = null;
		this.highlight = false;
		this.autocomplete = false;
		this.enabled = true;
		this.metadataGroup = "";
	}

	public String getCode() {
		return code;
	}

	public String getLocalcode() {
		return localcode;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public boolean isSortable() {
		return sortable;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isAdvancedSearch() {
		return advancedSearch;
	}

	public boolean isFacet() {
		return facet;
	}

	public String getReference() {
		return reference;
	}

	public MetadataInputType getInput() {
		return input;
	}

	public MetadataValueType getValueType() {
		return valueType;
	}

	public MetadataSchemaVO getSchema() {
		return schema;
	}

	public boolean isMultivalue() {
		return multivalue;
	}

	public boolean isHighlight() {
		return highlight;
	}

	public boolean isAutocomplete() {
		return autocomplete;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getLabel() {
		return label;
	}

	public String getMetadataGroup() {
		return this.metadataGroup;
	}

	public void setLocalcode(String localcode) {
		this.localcode = localcode;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public void setAdvancedSearch(boolean advancedSearch) {
		this.advancedSearch = advancedSearch;
	}

	public void setFacet(boolean facet) {
		this.facet = facet;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public void setInput(MetadataInputType input) {
		this.input = input;
	}

	public void setValueType(MetadataValueType type) {
		this.valueType = type;
	}

	public void setSchema(MetadataSchemaVO metadataSchemaVO) {
		this.schema = metadataSchemaVO;
	}

	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	public void setAutocomplete(boolean autocomplete) {
		this.autocomplete = autocomplete;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setMetadataGroup(String metadataGroup) {
		this.metadataGroup = metadataGroup;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormMetadataVO other = (FormMetadataVO) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}

	/**
	 * Used by Vaadin to populate the header of the column in a table (since we use MetadataVO objects as property ids).
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		String toString;
		try {
			toString = getLabel();
		} catch (RuntimeException e) {
			toString = super.toString();
		}
		return toString;
	}

}
