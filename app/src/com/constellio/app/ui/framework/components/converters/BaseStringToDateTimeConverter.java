package com.constellio.app.ui.framework.components.converters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.constellio.app.ui.util.DateFormatUtils;
import com.vaadin.data.util.converter.StringToDateConverter;

public class BaseStringToDateTimeConverter extends StringToDateConverter {

	@Override
	protected DateFormat getFormat(Locale locale) {
		return new SimpleDateFormat(DateFormatUtils.DATE_TIME_FORMAT);
	}

}
