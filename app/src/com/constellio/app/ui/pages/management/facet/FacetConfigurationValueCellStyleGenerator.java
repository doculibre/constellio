package com.constellio.app.ui.pages.management.facet;

import java.util.List;

import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;

public class FacetConfigurationValueCellStyleGenerator implements CellStyleGenerator {

	private List<Integer> invalidValues;

	public FacetConfigurationValueCellStyleGenerator(List<Integer> invalidValues) {
		this.invalidValues = invalidValues;
	}

	@Override
	public String getStyle(Table source, Object itemId, Object propertyId) {
		if (propertyId != null) {
			String property = (String) propertyId;
			if (property.equals("value")) {
				Integer id = (Integer) itemId;
				return isInvalid(id) ? "error" : null;
			} else {
				return null;
			}
		}
		return null;
	}

	private boolean isInvalid(Integer itemId) {
		for (Integer invalid : invalidValues) {
			if (invalid.equals(itemId)) {
				return true;
			}
		}

		return false;
	}
}
