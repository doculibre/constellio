package com.constellio.app.ui.framework.components.converters;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.Resource;

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
			caption = SchemaCaptionUtils.getCaptionForRecordId(value);
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

	public Resource getIcon(String recordId, boolean expanded) {
		return FileIconUtils.getIconForRecordId(recordId, expanded);
	}

}
