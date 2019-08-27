package com.constellio.app.ui.framework.components.converters;

import com.vaadin.data.util.converter.StringToDoubleConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

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

	@Override
	protected Number convertToNumber(String value, Class<? extends Number> targetType, Locale locale)
			throws ConversionException {
		if (value == null) {
			return null;
		}

		try {
			String numberDotFormat = value.replace(",", ".");
			return Double.parseDouble(numberDotFormat);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
