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

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

public class FacetSelectionsFactory implements StructureFactory {
	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		FacetSelections facetSelections = new FacetSelections();
		facetSelections.setFacetField(readString(stringTokenizer));
		facetSelections.setSelectedValues(readSet(stringTokenizer));
		facetSelections.dirty = false;
		return facetSelections;
	}

	@Override
	public String toString(ModifiableStructure structure) {

		FacetSelections facetSelections = (FacetSelections) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, facetSelections.getFacetField() == null ?
				NULL :
				facetSelections.getFacetField());
		if (facetSelections.getSelectedValues() != null) {
			for (String selectedValue : facetSelections.getSelectedValues()) {
				writeString(stringBuilder, selectedValue == null ?
						NULL :
						selectedValue);
			}
		} else {
			writeString(stringBuilder, null);
		}
		return stringBuilder.toString();
	}

	private String readString(StringTokenizer stringTokenizer) {
		String value = stringTokenizer.nextToken();
		if (NULL.equals(value)) {
			return null;
		} else {
			return value.replace("~~~", ":");
		}
	}

	private Set<String> readSet(StringTokenizer stringTokenizer) {
		Set<String> selectedValues = new HashSet<>();
		while (stringTokenizer.hasMoreTokens()) {
			try {
				String value = stringTokenizer.nextToken();
				if (NULL.equals(value)) {
					return selectedValues;
				} else {
					selectedValues.add(value.replace("~~~", ":"));
				}
			} catch (Exception e) {
				return selectedValues;
			}
		}
		return selectedValues;
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(":");
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value.replace(":", "~~~"));
		}
	}
}
