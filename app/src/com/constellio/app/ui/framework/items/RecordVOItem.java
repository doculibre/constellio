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
package com.constellio.app.ui.framework.items;

import java.util.Collection;
import java.util.List;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;

@SuppressWarnings("serial")
public class RecordVOItem implements Item {

	final RecordVO recordVO;

	public RecordVOItem(RecordVO recordVO) {
		super();
		this.recordVO = recordVO;
	}

	public RecordVO getRecord() {
		return recordVO;
	}

	@Override
	public Property<?> getItemProperty(Object id) {
		Property<?> itemProperty;
		if (id instanceof MetadataVO) {
			final MetadataVO metadata = (MetadataVO) id;
			itemProperty = new AbstractProperty<Object>() {
				@Override
				public Object getValue() {
					return getRecord().get(metadata);
				}

				@Override
				public void setValue(Object newValue)
						throws com.vaadin.data.Property.ReadOnlyException {
					getRecord().set(metadata, newValue);
				}

				@Override
				public Class<? extends Object> getType() {
					Class<?> propertyType;
					if (metadata.isMultivalue()) {
						propertyType = List.class;
					} else {
						propertyType = metadata.getJavaType();
					}
					return propertyType;
				}
			};
		} else {
			itemProperty = null;
		}
		return itemProperty;
	}

	@Override
	public Collection<?> getItemPropertyIds() {
		return getRecord().getMetadatas();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean addItemProperty(Object id, Property property)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean removeItemProperty(Object id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("TODO");
	}

}
