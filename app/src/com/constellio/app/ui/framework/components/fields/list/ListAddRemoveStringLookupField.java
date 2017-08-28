package com.constellio.app.ui.framework.components.fields.list;

import java.util.List;

import com.constellio.app.ui.framework.components.fields.lookup.StringLookupField;

@SuppressWarnings("unchecked")
public class ListAddRemoveStringLookupField extends ListAddRemoveField<String, StringLookupField> {
	
	private List<String> options;
	
	public ListAddRemoveStringLookupField(List<String> options) {
		this.options = options;
	}

	@Override
	protected StringLookupField newAddEditField() {
		return new StringLookupField(options, getItemConverter());
	}

}
