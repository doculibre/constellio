package com.constellio.app.ui.framework.components.converters;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.util.converter.StringToDoubleConverter;

public class BaseStringToDoubleConverter extends StringToDoubleConverter {

	@Override
	public String convertToPresentation(Double value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
        if (value == null) {
            return null;
        }
        String stringValue = value.toString().replace(" ", "").replace(",", "");
        if (stringValue.endsWith(".0")) {
        	stringValue = StringUtils.substringBefore(stringValue, ".0");
        }
        return stringValue;
	}


}
