package com.constellio.app.ui.framework.items;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class RecordVOItem implements Item {

	final RecordVO recordVO;
	final SearchResultVO searchResultVO;

	public RecordVOItem(RecordVO recordVO) {
		super();
		this.recordVO = recordVO;
		this.searchResultVO = null;
	}

	public RecordVOItem(SearchResultVO searchResultVO) {
		super();
		this.recordVO = searchResultVO.getRecordVO();
		this.searchResultVO = searchResultVO;
	}

	public RecordVO getRecord() {
		return recordVO;
	}

	public SearchResultVO getSearchResult() {
		return searchResultVO;
	}

	public boolean isDeleted() {
		if (searchResultVO != null) {
			return searchResultVO.isDeleted();
		} else {
			return false;
		}
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
		if (!isDeleted()) {
			return getRecord().getMetadatas();
		} else {
			return Collections.emptyList();
		}
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
