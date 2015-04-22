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
