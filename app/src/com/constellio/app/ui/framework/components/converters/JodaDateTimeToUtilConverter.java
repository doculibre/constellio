package com.constellio.app.ui.framework.components.converters;

import com.vaadin.data.util.converter.Converter;
import org.joda.time.LocalDateTime;

import java.util.Date;
import java.util.Locale;

@SuppressWarnings("serial")
public class JodaDateTimeToUtilConverter implements Converter<Date, LocalDateTime> {

	@Override
	public LocalDateTime convertToModel(Date value, Class<? extends LocalDateTime> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value != null ? new LocalDateTime(value) : null;
	}

	@Override
	public Date convertToPresentation(LocalDateTime value, Class<? extends Date> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value != null ? value.toDate() : null;
	}

	@Override
	public Class<LocalDateTime> getModelType() {
		return LocalDateTime.class;
	}

	@Override
	public Class<Date> getPresentationType() {
		return Date.class;
	}

}
