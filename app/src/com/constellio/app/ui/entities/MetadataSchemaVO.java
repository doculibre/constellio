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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.constellio.app.ui.application.ConstellioUI;

@SuppressWarnings("serial")
public class MetadataSchemaVO implements Serializable {

	final String code;

	final String collection;

	final List<MetadataVO> metadatas = new ArrayList<MetadataVO>();

	final Map<Locale, String> labels;

	public MetadataSchemaVO(String code, String collection, Map<Locale, String> labels) {
		super();
		this.code = code;
		this.collection = collection;
		this.labels = labels;
	}

	public String getCode() {
		return code;
	}

	public String getCollection() {
		return collection;
	}

	public List<MetadataVO> getMetadatas() {
		return metadatas;
	}

	public MetadataVO getMetadata(String metadataCode) {
		MetadataVO match = null;
		String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
		for (MetadataVO schemaMetadata : metadatas) {
			String schemaMetadataCode = schemaMetadata.getCode();
			String schemaMetadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(schemaMetadataCode);
			if (schemaMetadataCodeWithoutPrefix.equals(metadataCodeWithoutPrefix)) {
				match = schemaMetadata;
				break;
			}
		}
		return match;
	}

	public Map<Locale, String> getLabels() {
		return labels;
	}

	public String getLabel(Locale locale) {
		return labels.get(locale);
	}

	public String getLabel() {
		return labels.get(ConstellioUI.getCurrentSessionContext().getCurrentLocale());
	}

	public void setLabel(Locale locale, String label) {
		labels.put(locale, label);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((collection == null) ? 0 : collection.hashCode());
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
		MetadataSchemaVO other = (MetadataSchemaVO) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (collection == null) {
			if (other.collection != null)
				return false;
		} else if (!collection.equals(other.collection))
			return false;
		return true;
	}

	/**
	 * Used by Vaadin to populate the header of the column in a table (since we use MetadataVO objects as property ids).
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String toString;
		try {
			toString = getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale());
		} catch (RuntimeException e) {
			toString = code;
		}
		return toString;
	}

}
