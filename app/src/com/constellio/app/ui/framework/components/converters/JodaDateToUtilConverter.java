package com.constellio.app.ui.framework.components.converters;

import java.util.Date;
import java.util.Locale;

import org.joda.time.LocalDate;

import com.vaadin.data.util.converter.Converter;

@SuppressWarnings("serial")
public class JodaDateToUtilConverter implements Converter<Date, LocalDate> {

	@Override
	public LocalDate convertToModel(Date value, Class<? extends LocalDate> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value != null ? new LocalDate(value) : null;
	}

	@Override
	public Date convertToPresentation(LocalDate value, Class<? extends Date> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value != null ? value.toDate() : null;
	}

	@Override
	public Class<LocalDate> getModelType() {
		return LocalDate.class;
	}

	@Override
	public Class<Date> getPresentationType() {
		return Date.class;
	}

}
