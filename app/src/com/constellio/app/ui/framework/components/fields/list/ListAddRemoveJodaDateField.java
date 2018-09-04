package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.vaadin.ui.DateField;
import org.joda.time.LocalDate;

@SuppressWarnings("unchecked")
public class ListAddRemoveJodaDateField extends ListAddRemoveField<LocalDate, DateField> {

	public ListAddRemoveJodaDateField() {
		super();
		setItemConverter(new JodaDateToStringConverter());
	}

	@Override
	protected DateField newAddEditField() {
		return new JodaDateField();
	}

}
