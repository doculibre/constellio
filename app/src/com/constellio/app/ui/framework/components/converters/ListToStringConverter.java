package com.constellio.app.ui.framework.components.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.util.DateFormatUtils;
import com.vaadin.data.util.converter.Converter;

@SuppressWarnings("serial")
public class ListToStringConverter implements Converter<String, List<String>> {
	
	private String separator;
	
	public ListToStringConverter() {
		this(", ");
	}
	public ListToStringConverter(String separator) {
		this.separator = separator;
	}
	
	@Override
	public List<String> convertToModel(String value, Class<? extends List<String>> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		List<String> listValue;
		if (value != null) {
			listValue = new ArrayList<>();
			String[] tokens = StringUtils.split(value, separator);
			for (String token : tokens) {
				listValue.add(token);
			}
		} else {
			listValue = null;
		}
		return listValue;
	}
	
	protected String getPattern() {
		return DateFormatUtils.DATE_TIME_FORMAT;
	}

	@Override
	public String convertToPresentation(List<String> value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		return value != null ? StringUtils.join(value.iterator(), separator) : null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getModelType() {
		return List.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}

}
