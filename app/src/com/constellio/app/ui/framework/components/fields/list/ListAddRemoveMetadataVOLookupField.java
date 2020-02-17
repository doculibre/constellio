package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.fields.lookup.MetadataVOLookupField;

import java.util.List;

@SuppressWarnings("unchecked")
public class ListAddRemoveMetadataVOLookupField extends ListAddRemoveField<MetadataVO, MetadataVOLookupField> {

	private List<MetadataVO> options;

	public ListAddRemoveMetadataVOLookupField(List<MetadataVO> options) {
		this.options = options;
	}

	@Override
	protected MetadataVOLookupField newAddEditField() {
		return new MetadataVOLookupField(options, getItemConverter());
	}

}
