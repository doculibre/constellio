package com.constellio.app.ui.framework.components.converters;

import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.vaadin.data.util.converter.Converter;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class RecordIdToCaptionWithNiceTitleConverter implements Converter<String, String> {

	@Override
	public String convertToModel(String value, Class<? extends String> targetType, Locale locale)
			throws ConversionException {
		return value;
	}

	@Override
	public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale)
			throws ConversionException {
		String caption;
		if (StringUtils.isNotBlank(value)) {
			String niceTitle = SchemaCaptionUtils.getNiceTitleForRecordId(value, locale);
			if (StringUtils.isNotBlank(niceTitle)) {
				caption = "<span title=\"" + StringEscapeUtils.escapeHtml(niceTitle) + "\">" + SchemaCaptionUtils.getCaptionForRecordId(value, locale) + "</span>";
			} else {
				caption = SchemaCaptionUtils.getCaptionForRecordId(value, locale);
			}
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
