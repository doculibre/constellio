package com.constellio.model.utils;

import java.text.ParseException;

import javax.swing.text.MaskFormatter;

import com.constellio.model.utils.MaskUtilsException.MaskUtilsException_InvalidValue;

public class MaskUtils {

	public static String format(String mask, String rawValue)
			throws MaskUtilsException {

		if (rawValue == null || mask == null || isValid(mask, rawValue)) {
			return rawValue;
		}

		try {

			MaskFormatter maskFormatter = newMaskFormatter(mask);
			maskFormatter.setValueContainsLiteralCharacters(false);
			String formattedValue = maskFormatter.valueToString(rawValue);

			if (!isValid(mask, formattedValue)) {
				throw new MaskUtilsException_InvalidValue(mask, rawValue);
			}

			return formattedValue;

		} catch (ParseException e) {
			throw new MaskUtilsException_InvalidValue(mask, rawValue, e);
		}

	}

	public static boolean isValid(String mask, String value) {
		try {
			validate(mask, value);
			return true;
		} catch (MaskUtilsException e) {
			return false;
		}
	}

	public static void validate(String mask, String formattedValue)
			throws MaskUtilsException {

		if (formattedValue == null) {
			return;
		}

		MaskFormatter maskFormatter = newMaskFormatter(mask);
		maskFormatter.setValueContainsLiteralCharacters(false);
		maskFormatter.setAllowsInvalid(false);
		try {
			Object rawValue = maskFormatter.stringToValue(formattedValue);
			String newFormattedValue = maskFormatter.valueToString(rawValue);
			if (!formattedValue.equals(newFormattedValue)) {
				throw new MaskUtilsException_InvalidValue(mask, formattedValue);
			}
		} catch (ParseException e) {
			throw new MaskUtilsException_InvalidValue(mask, formattedValue, e);
		}
	}

	private static MaskFormatter newMaskFormatter(String mask)
			throws MaskUtilsException {

		try {
			return new MaskFormatter(mask);
		} catch (ParseException e) {
			throw new MaskUtilsException.MaskUtilsException_InvalidMask(mask);
		}
	}
}
