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
package com.constellio.app.modules.rm.ui.components.facet;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.model.entities.records.wrappers.Facet;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.OptionGroup;

public class FacetFieldFactory extends MetadataFieldFactory {

	private ComboBox dataStoreCode;
	private OptionGroup facetType;

	public FacetFieldFactory(ComboBox dataStoreCode, OptionGroup facetType) {
		this.dataStoreCode = dataStoreCode;
		this.facetType = facetType;
	}

	@Override
	public Field<?> build(MetadataVO metadata) {
		Field<?> field;
		String metadataCode = metadata.getCode();
		if (metadataCode.endsWith(Facet.FIELD_DATA_STORE_CODE)) {
			field = dataStoreCode;
		} else if (metadataCode.endsWith(Facet.ORDER)) {
			field = null;
		} else if (metadataCode.endsWith(Facet.PAGES)) {
			field = null;
		} else if (metadataCode.endsWith(Facet.FACET_TYPE)) {
			field = facetType;
		} else {
			field = super.build(metadata);
		}

		return field;
	}
}
