package com.constellio.model.utils;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.EnumWithSmallCode;

import java.util.ArrayList;
import java.util.List;

public class EnumWithSmallCodeUtils {

	public static String toSmallCode(Enum<?> enumValue) {
		return toSmallCode((EnumWithSmallCode) enumValue);
	}

	public static String toSmallCode(EnumWithSmallCode enumValue) {
		return enumValue == null ? null : enumValue.getCode();
	}

	@SuppressWarnings("unchecked")
	public static Enum<?> toEnum(Class<? extends Enum<?>> enumClass, String code) {
		if (code == null) {
			return null;
		}
		return (Enum<?>) toEnumWithSmallCode((Class<? extends EnumWithSmallCode>) enumClass, code);
	}

	private static List<EnumWithSmallCode> enumConstants;

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
		if (enumValues == null) {
			return null;
		}
		List<String> codes = new ArrayList<>();
		if (enumValues != null) {
			for (Enum<?> enumValue : enumValues) {
				if (enumValue != null) {
					codes.add(toSmallCode(enumValue));
				} else {
					codes.add(null);
				}
			}
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
