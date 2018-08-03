package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.BaseStringToDateTimeConverter;
import com.constellio.app.ui.framework.components.fields.date.BaseDateTimeField;
import com.vaadin.ui.DateField;

import java.util.Date;

@SuppressWarnings("unchecked")
public class ListAddRemoveDateTimeField extends ListAddRemoveField<Date, DateField> {

	public ListAddRemoveDateTimeField() {
		super();
		setItemConverter(new BaseStringToDateTimeConverter());
	}

	@Override
	protected DateField newAddEditField() {
		return new BaseDateTimeField();
	}

}
