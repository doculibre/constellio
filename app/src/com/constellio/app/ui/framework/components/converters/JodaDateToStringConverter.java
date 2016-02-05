package com.constellio.app.ui.framework.components.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.LocalDate;

import com.constellio.app.ui.util.DateFormatUtils;
import com.vaadin.data.util.converter.Converter;

@SuppressWarnings("serial")
public class JodaDateToStringConverter implements Converter<String, LocalDate> {
	
	@Override
	public LocalDate convertToModel(String value, Class<? extends LocalDate> targetType, Locale locale)
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
		return value != null ? new LocalDate(utilDate) : null;
	}
	
	protected String getPattern() {
		return DateFormatUtils.DATE_FORMAT;
	}

	@Override
	public String convertToPresentation(LocalDate value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value != null ? value.toString(getPattern()) : null;
	}

	@Override
	public Class<LocalDate> getModelType() {
		return LocalDate.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}

}
