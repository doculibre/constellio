package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

import org.joda.time.LocalDate;

import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.vaadin.ui.Label;

public class LocalDateLabel extends Label {
	public LocalDateLabel(LocalDate date) {
		JodaDateToStringConverter converter = new JodaDateToStringConverter();
		setValue(converter.convertToPresentation(date, String.class, getCurrentSessionContext().getCurrentLocale()));
	}
}
