package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.vaadin.ui.Label;
import org.joda.time.LocalDate;

import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

public class LocalDateLabel extends Label {
	public LocalDateLabel(LocalDate date) {
		JodaDateToStringConverter converter = new JodaDateToStringConverter();
		setValue(converter.convertToPresentation(date, String.class, getCurrentSessionContext().getCurrentLocale()));
	}
}
