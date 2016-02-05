package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.vaadin.data.util.converter.StringToIntegerConverter;

@SuppressWarnings("unchecked")
public class ListAddRemoveIntegerField extends ListAddRemoveField<Integer, BaseIntegerField> {

	public ListAddRemoveIntegerField() {
		super();
		setItemConverter(new StringToIntegerConverter());
	}

	@Override
	protected BaseIntegerField newAddEditField() {
		return new BaseIntegerField();
	}

}
