package com.constellio.app.ui.pages.search.criteria;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class FacetSelectionsFactory implements CombinedStructureFactory {
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
