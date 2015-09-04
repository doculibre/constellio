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
