package com.constellio.model.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MaskFormatter;

import com.constellio.model.utils.MaskUtilsException.MaskUtilsException_InvalidValue;

public class MaskUtils {

	//	public static String format(String mask, String rawValue)
	//			throws MaskUtilsException {
	//
	//		if (rawValue == null || mask == null || isValid(mask, rawValue)) {
	//			return rawValue;
	//		}
	//
	//		try {
	//
	//			MaskFormatter maskFormatter = newMaskFormatter(mask);
	//			maskFormatter.setValueContainsLiteralCharacters(false);
	//			String formattedValue = maskFormatter.valueToString(rawValue);
	//
	//			if (!isValid(mask, formattedValue)) {
	//				throw new MaskUtilsException_InvalidValue(mask, rawValue);
	//			}
	//
	//			return formattedValue;
	//
	//		} catch (ParseException e) {
	//			throw new MaskUtilsException_InvalidValue(mask, rawValue, e);
	//		}
	//
	//	}

	private static Map<String, Pattern> maskRegexPatterns = new HashMap<>();

	public static boolean isValid(String mask, String value) {
		try {
			validate(mask, value);
			return true;
		} catch (MaskUtilsException e) {
			return false;
		}
	}

	private static Pattern buildRegex(String mask) {
		Pattern maskRegexPattern = maskRegexPatterns.get(mask);
		if (maskRegexPattern == null) {
			String regex = mask.replace("\\9", "__ZE_NEUF__").replace("\\A", "__ZE_FIRST_LETTER__")
					.replace("\\*", "__ZE_FLOCON__")
					.replace("9", "\\d").replace("A", "[A-Za-z]").replace("*", "[A-Za-z0-9]").replace("(", "\\(")
					.replace(")", "\\)")
					.replace("__ZE_NEUF__", "9").replace("__ZE_FIRST_LETTER__", "A").replace("__ZE_FLOCON__", "\\*");
			maskRegexPattern = Pattern.compile(regex);
			maskRegexPatterns.put(mask, maskRegexPattern);
		}
		return maskRegexPattern;
	}

	public static void validate(String mask, String formattedValue)
			throws MaskUtilsException {

		Pattern pattern = buildRegex(mask);
		Matcher matcher = pattern.matcher(formattedValue);

		boolean valid = matcher.matches();
		if (!valid) {
			throw new MaskUtilsException_InvalidValue(mask, formattedValue);
		}
		//
		//		if (formattedValue == null) {
		//			return;
		//		}
		//
		//		MaskFormatter maskFormatter = newMaskFormatter(mask);
		//		maskFormatter.setValueContainsLiteralCharacters(false);
		//		maskFormatter.setAllowsInvalid(false);
		//		try {
		//			Object rawValue = maskFormatter.stringToValue(formattedValue);
		//			String newFormattedValue = maskFormatter.valueToString(rawValue);
		//			if (!formattedValue.equals(newFormattedValue)) {
		//				throw new MaskUtilsException_InvalidValue(mask, formattedValue);
		//			}
		//		} catch (ParseException e) {
		//			throw new MaskUtilsException_InvalidValue(mask, formattedValue, e);
		//		}
	}

	private static MaskFormatter newMaskFormatter(String mask)
			throws MaskUtilsException {

		try {
			return new MaskFormatter(mask);
		} catch (ParseException e) {
			throw new MaskUtilsException.MaskUtilsException_InvalidMask(mask);
		}
	}

	static Pattern onlyNumbers = Pattern.compile("[9-9]+");

	public static String format(String inputMask, String value) {
		if (inputMask == null) {
			return value;
		} else {
			if (value == null) {
				return null;
			} else {
				if (onlyNumbers.matcher(inputMask).matches()) {
					String concat = "000000000000000" + value;
					return concat.substring(concat.length() - inputMask.length());
				} else {
					return value;
				}
			}
		}
	}
}
