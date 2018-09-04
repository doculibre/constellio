package com.constellio.app.ui.framework.components.converters;

import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.vaadin.data.util.converter.Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class RecordIdToCaptionConverter implements Converter<String, String> {

	@Override
	public String convertToModel(String value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value;
	}

	@Override
	public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		String caption;
		if (StringUtils.isNotBlank(value)) {
			caption = SchemaCaptionUtils.getCaptionForRecordId(value, locale);
		} else {
			caption = "";
		}
		return caption;
	}

	@Override
	public Class<String> getModelType() {
		return String.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}

}
