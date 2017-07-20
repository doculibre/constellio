package com.constellio.app.ui.framework.components.converters;

import java.util.Locale;

import com.vaadin.data.util.converter.StringToIntegerConverter;

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
