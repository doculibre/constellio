package com.constellio.app.ui.framework.components.converters;

import com.constellio.app.ui.util.DateFormatUtils;
import com.vaadin.data.util.converter.StringToDateConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class BaseStringToDateConverter extends StringToDateConverter {

	@Override
	protected DateFormat getFormat(Locale locale) {
		return new SimpleDateFormat(DateFormatUtils.getDateFormat());
	}

}
