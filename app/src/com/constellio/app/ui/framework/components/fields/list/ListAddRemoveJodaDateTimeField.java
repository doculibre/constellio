package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.fields.date.JodaDateTimeField;
import com.vaadin.ui.DateField;
import org.joda.time.LocalDateTime;

@SuppressWarnings("unchecked")
public class ListAddRemoveJodaDateTimeField extends ListAddRemoveField<LocalDateTime, DateField> {

	public ListAddRemoveJodaDateTimeField() {
		super();
		setItemConverter(new JodaDateTimeToStringConverter());
	}

	@Override
	protected DateField newAddEditField() {
		return new JodaDateTimeField();
	}

}
