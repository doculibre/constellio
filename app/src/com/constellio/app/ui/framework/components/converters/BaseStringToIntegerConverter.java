package com.constellio.app.ui.framework.components.converters;

import com.vaadin.data.util.converter.StringToIntegerConverter;

import java.util.Locale;

public class BaseStringToIntegerConverter extends StringToIntegerConverter {

	@Override
	public String convertToPresentation(Integer value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		if (value == null) {
			return null;
		}
		String stringValue = value.toString().replace(" ", "");
		return stringValue;
	}


}
