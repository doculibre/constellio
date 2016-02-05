package com.constellio.app.ui.pages.management.extractors.converters;

import java.util.Locale;

import com.constellio.app.ui.pages.management.extractors.entities.RegexConfigVO;
import com.vaadin.data.util.converter.Converter;

public class RegexConfigVOToStringConverter implements Converter<String, RegexConfigVO> {

	@Override
	public RegexConfigVO convertToModel(String value, Class<? extends RegexConfigVO> targetType, Locale locale)
			throws ConversionException {
		return null;
	}

	@Override
	public String convertToPresentation(RegexConfigVO value, Class<? extends String> targetType, Locale locale)
			throws ConversionException {
		String presentation = null;
		if (value != null) {
			presentation = value.getInputMetadata();
		}
		return presentation;
	}



	@Override
	public Class<RegexConfigVO> getModelType() {
		return RegexConfigVO.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}
}
