package com.constellio.app.ui.framework.components.converters;

import java.util.Locale;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import com.vaadin.data.util.converter.Converter;

public class StringToEnumWithSmallCodeConverter<T extends EnumWithSmallCode> implements Converter<String, EnumWithSmallCode> {
	
	private Class<T> enumWithSmallCodeClass;
	
	public StringToEnumWithSmallCodeConverter(Class<T> enumWithSmallCodeClass) {
		this.enumWithSmallCodeClass = enumWithSmallCodeClass;
	}

	@Override
	public EnumWithSmallCode convertToModel(String value, Class<? extends EnumWithSmallCode> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return EnumWithSmallCodeUtils.toEnumWithSmallCode(enumWithSmallCodeClass, value);
	}

	@Override
	public String convertToPresentation(EnumWithSmallCode value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return EnumWithSmallCodeUtils.toSmallCode(value);
	}

	@Override
	public Class<EnumWithSmallCode> getModelType() {
		return EnumWithSmallCode.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}

}
