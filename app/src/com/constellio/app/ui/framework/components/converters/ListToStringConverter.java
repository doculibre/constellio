/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
