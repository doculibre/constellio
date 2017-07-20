package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.BaseStringToIntegerConverter;
import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;

@SuppressWarnings("unchecked")
public class ListAddRemoveIntegerField extends ListAddRemoveField<Integer, BaseIntegerField> {

	public ListAddRemoveIntegerField() {
		super();
		setItemConverter(new BaseStringToIntegerConverter());
	}

	@Override
	protected BaseIntegerField newAddEditField() {
		return new BaseIntegerField();
	}

}
