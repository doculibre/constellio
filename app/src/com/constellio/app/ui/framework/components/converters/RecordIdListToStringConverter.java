package com.constellio.app.ui.framework.components.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.util.converter.Converter;

@SuppressWarnings("serial")
public class RecordIdListToStringConverter implements Converter<String, List<String>> {

	private String separator;

	private RecordIdToCaptionConverter recordIdToCaptionConverter = new RecordIdToCaptionConverter();

	public RecordIdListToStringConverter() {
		this(", ");
	}
	public RecordIdListToStringConverter(String separator) {
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

	@Override
	public String convertToPresentation(List<String> value, Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		String presentation;
		if (value != null) {
			StringBuffer sb = new StringBuffer();
			for (String recordId : value) {
				if (sb.length() > 0) {
					sb.append(separator);
				}
				String caption = recordIdToCaptionConverter.convertToPresentation(recordId, String.class, locale);
				sb.append(caption);
			}
			presentation = sb.toString();
		} else {
			presentation = null;
		}
		return presentation;
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
