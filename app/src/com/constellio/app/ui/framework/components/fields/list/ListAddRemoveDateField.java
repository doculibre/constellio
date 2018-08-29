package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.BaseStringToDateConverter;
import com.constellio.app.ui.framework.components.fields.date.BaseDateField;
import com.vaadin.ui.DateField;

import java.util.Date;

@SuppressWarnings("unchecked")
public class ListAddRemoveDateField extends ListAddRemoveField<Date, DateField> {

	public ListAddRemoveDateField() {
		super();
		setItemConverter(new BaseStringToDateConverter());
	}

	@Override
	protected DateField newAddEditField() {
		return new BaseDateField();
	}

}
