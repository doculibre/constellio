package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.BaseStringToDoubleConverter;
import com.constellio.app.ui.framework.components.fields.number.BaseDoubleField;

@SuppressWarnings("unchecked")
public class ListAddRemoveDoubleField extends ListAddRemoveField<Double, BaseDoubleField> {

	public ListAddRemoveDoubleField() {
		super();
		setItemConverter(new BaseStringToDoubleConverter());

	}

	@Override
	protected BaseDoubleField newAddEditField() {
		return new BaseDoubleField();
	}

}
