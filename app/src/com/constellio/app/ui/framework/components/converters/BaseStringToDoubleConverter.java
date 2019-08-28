package com.constellio.app.ui.framework.components.converters;

import com.vaadin.data.util.converter.StringToDoubleConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

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

	private Double convertToDouble(String value) {
		if (value == null) {
			return null;
		}

		String numberDotFormat = value.replace(",", ".");
		return Double.parseDouble(numberDotFormat);
	}

	@Override
	public Double convertToModel(String value, Class<? extends Double> targetType, Locale locale)
			throws ConversionException {
		try {
			return StringUtils.isBlank(value) ? null : convertToDouble(value);
		} catch (NumberFormatException e) {
			throw new ConversionException($("AdvancedSearchView.invalidDoubleFormat"));
		}
	}
}
