package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.fields.number.BaseDoubleField;
import com.vaadin.data.util.converter.StringToDoubleConverter;

@SuppressWarnings("unchecked")
public class ListAddRemoveDoubleField extends ListAddRemoveField<Double, BaseDoubleField> {

	public ListAddRemoveDoubleField() {
		super();
		setItemConverter(new StringToDoubleConverter());
	}

	@Override
	protected BaseDoubleField newAddEditField() {
		return new BaseDoubleField();
	}

}
