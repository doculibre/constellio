package com.constellio.app.ui.framework.components.converters;

import com.constellio.model.entities.EnumWithSmallCode;
import com.vaadin.data.util.converter.Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class EnumWithSmallCodeToCaptionConverter implements Converter<String, String> {

	private Class<? extends EnumWithSmallCode> enumWithSmallCodeClass;

	public EnumWithSmallCodeToCaptionConverter(Class<? extends EnumWithSmallCode> enumWithSmallCodeClass) {
		this.enumWithSmallCodeClass = enumWithSmallCodeClass;
	}

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
			caption = $(enumWithSmallCodeClass.getSimpleName() + "." + value);
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
