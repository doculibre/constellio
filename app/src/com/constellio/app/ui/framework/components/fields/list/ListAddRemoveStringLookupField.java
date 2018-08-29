package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.fields.lookup.StringLookupField;

import java.util.List;

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
