package com.constellio.app.ui.framework.items;

import java.util.Collection;
import java.util.List;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata;
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
		if (id instanceof String) {
			try {
				MetadataVO metadataVO = recordVO.getMetadata((String) id);
				id = metadataVO;
			} catch (RecordVORuntimeException_NoSuchMetadata e) {
				// Ignore
			}
		}
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
