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

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.constellio.model.entities.EnumWithSmallCode;
import com.vaadin.data.util.converter.Converter;

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
