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
package com.constellio.model.utils;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.EnumWithSmallCode;

public class EnumWithSmallCodeUtils {

	public static String toSmallCode(Enum<?> enumValue) {
		return toSmallCode((EnumWithSmallCode) enumValue);
	}

	public static String toSmallCode(EnumWithSmallCode enumValue) {
		return enumValue == null ? null : enumValue.getCode();
	}

	@SuppressWarnings("unchecked")
	public static Enum<?> toEnum(Class<? extends Enum<?>> enumClass, String code) {
		return (Enum<?>) toEnumWithSmallCode((Class<? extends EnumWithSmallCode>) enumClass, code);
	}

	public static EnumWithSmallCode toEnumWithSmallCode(Class<? extends EnumWithSmallCode> enumClass, String code) {
		if (code == null) {
			return null;
		}
		List<EnumWithSmallCode> enumConstants = toEnumWithSmallCodeConstants(enumClass);

		for (EnumWithSmallCode enumConstant : enumConstants) {
			String anEnumValueCode = enumConstant.getCode();
			if (anEnumValueCode.equals(code)) {
				return enumConstant;
			}
		}

		throw new ImpossibleRuntimeException("No such enum value with code '" + code + "' in enum '" + enumClass + "'");
	}

	public static List<String> toSmallCodeList(List<Enum<?>> enumValues) {
		List<String> codes = new ArrayList<>();
		for (Enum<?> enumValue : enumValues) {
			codes.add(toSmallCode(enumValue));
		}
		return codes;
	}

	public static List<String> toSmallCodeList(Class<? extends Enum<?>> enumClass) {
		List<String> codes = new ArrayList<>();
		List<EnumWithSmallCode> enumValues = toEnumWithSmallCodeConstants((Class) enumClass);
		for (EnumWithSmallCode enumValue : enumValues) {
			codes.add(toSmallCode(enumValue));
		}
		return codes;
	}

	public static List<Enum<?>> toEnumList(Class<? extends Enum<?>> enumClass, List<String> codes) {
		List<Enum<?>> enumValues = new ArrayList<>();
		for (String code : codes) {
			enumValues.add(toEnum(enumClass, code));
		}
		return enumValues;
	}

	public static List<EnumWithSmallCode> toEnumWithSmallCodeConstants(
			Class<? extends EnumWithSmallCode> enumWithSmallCodeClass) {
		if (!enumWithSmallCodeClass.isEnum()) {
			throw new IllegalArgumentException(enumWithSmallCodeClass.getName() + " is not an enum");
		} else if (!EnumWithSmallCode.class.isAssignableFrom(enumWithSmallCodeClass)) {
			throw new IllegalArgumentException(
					enumWithSmallCodeClass.getName() + " does not implement " + EnumWithSmallCode.class.getName());
		}

		List<EnumWithSmallCode> enumConstants = new ArrayList<EnumWithSmallCode>();
		for (Object enumConstant : enumWithSmallCodeClass.getEnumConstants()) {
			if (enumConstant instanceof EnumWithSmallCode) {
				EnumWithSmallCode enumWithSmallCode = (EnumWithSmallCode) enumConstant;
				enumConstants.add(enumWithSmallCode);
			}
		}
		return enumConstants;
	}

}
