package com.constellio.app.ui.framework.components.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.LocalDateTime;

import com.constellio.app.ui.util.DateFormatUtils;
import com.vaadin.data.util.converter.Converter;

@SuppressWarnings("serial")
public class JodaDateTimeToStringConverter implements Converter<String, LocalDateTime> {
	@Override
	public LocalDateTime convertToModel(String value, Class<? extends LocalDateTime> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		Date utilDate;
		if (value != null) {
			try {
				utilDate = new SimpleDateFormat(getPattern()).parse(value);
			} catch (ParseException e) {
				throw new ConversionException(e);
			}
		} else {
			utilDate = null;
		}
		return value != null ? new LocalDateTime(utilDate) : null;
	}

	protected String getPattern() {
		return DateFormatUtils.getDateTimeFormat();
	}

	@Override
	public String convertToPresentation(LocalDateTime value, Class<? extends String> targetType, Locale locale)
			throws ConversionException {
		return DateFormatUtils.format(value);
	}

	@Override
	public Class<LocalDateTime> getModelType() {
		return LocalDateTime.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}
}
