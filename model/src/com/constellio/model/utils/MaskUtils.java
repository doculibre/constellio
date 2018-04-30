package com.constellio.model.utils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.text.MaskFormatter;

import org.apache.commons.lang3.StringUtils;

import com.constellio.model.utils.MaskUtilsException.MaskUtilsException_InvalidValue;

public class MaskUtils {
	
	public static final String MM_DD = "^((0[1-9]|1[0-2])\\/([01][1-9]|10|2[0-8]))|((0[13-9]|1[0-2])\\/(29|30))|((0[13578]|1[0-2])\\/31)|02\\/29$";

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

	public static final String REGEX_STRICT_MASK_ITEMS = "[^A9]";

    private static Pattern REGEX_STRICT_MASK_PATTERN;
    private static Map<String, Pattern> maskRegexPatterns = new HashMap<>();
	private static Map<String, Pattern> maskStrictRegexPatterns = new HashMap<>();

	public static boolean isValid(String mask, String value) {
		try {
			validate(mask, value);
			return true;
		} catch (MaskUtilsException e) {
			return false;
		}
	}

	private static Pattern buildStrictMaskItemsRegex(){
	    if (REGEX_STRICT_MASK_PATTERN == null){
            REGEX_STRICT_MASK_PATTERN = Pattern.compile(REGEX_STRICT_MASK_ITEMS);
        }
        return REGEX_STRICT_MASK_PATTERN;
    }

	private static Pattern buildRegex(String mask) {
		Pattern maskRegexPattern = maskRegexPatterns.get(mask);
		if (maskRegexPattern == null) {
			try {
				String regex = mask.replace("\\9", "__ZE_NEUF__").replace("\\A", "__ZE_FIRST_LETTER__")
						.replace("\\*", "__ZE_FLOCON__")
						.replace("9", "\\d").replace("A", "[A-Za-z]").replace("*", "[A-Za-z0-9]").replace("(", "\\(")
						.replace(")", "\\)")
						.replace("__ZE_NEUF__", "9").replace("__ZE_FIRST_LETTER__", "A").replace("__ZE_FLOCON__", "\\*");
				maskRegexPattern = Pattern.compile(regex);
			} catch (PatternSyntaxException e) {
				try {
					maskRegexPattern = Pattern.compile(mask);
				} catch (PatternSyntaxException e2) {
					throw e;
				}
			}
			maskRegexPatterns.put(mask, maskRegexPattern);
		}
		return maskRegexPattern;
	}

	private static Pattern strictBuildRegex(String mask) {
		Pattern maskRegexPattern = maskStrictRegexPatterns.get(mask);

		if (maskRegexPattern == null) {
			String regex = mask.replace("9", "\\d").replace("A", "[A-Za-z]")
					.replace("__ZE_NEUF__", "9")
					.replace("__ZE_FIRST_LETTER__", "A");
			maskRegexPattern = Pattern.compile(regex);
			maskStrictRegexPatterns.put(mask, maskRegexPattern);
		}

		return maskRegexPattern;
	}

	public static void validate(String mask, String formattedValue)
			throws MaskUtilsException {
		if (StringUtils.isBlank(mask)) {
			return;
		}

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

	public static boolean strictValidate(String inputMask, String rawValue) {
        Pattern strictMaskItemsPattern = buildStrictMaskItemsRegex();
		Pattern pattern = strictBuildRegex(strictMaskItemsPattern.matcher(inputMask).replaceAll(""));
		Matcher matcher = pattern.matcher(rawValue);

		return matcher.matches();
	}

	public static String strictFormat(String inputMask, String rawValues) throws MaskUtilsException {
		String formattedString;

		if (strictValidate(inputMask, rawValues)) {
			formattedString = formatValueWithMask(inputMask, rawValues);
		} else {
			throw new MaskUtilsException.MaskUtilsException_InvalidValue(inputMask, rawValues);
		}

		return formattedString;
	}

    public static String strictFormatWithMissingValues(String inputMask, String rawValues) throws MaskUtilsException {
        String adjustedRawValue = adjustRawValue(inputMask, rawValues);
	    return strictFormat(inputMask,adjustedRawValue);
    }

    private static String adjustRawValue (String inputMask, String rawValue){
	    Pattern pattern = buildStrictMaskItemsRegex();
        String inputMaskWithoutStrictMaskItems = pattern.matcher(inputMask).replaceAll("");

        char[] maskAsChar = inputMaskWithoutStrictMaskItems.toCharArray();
        StringBuilder finalAdjustedItem = new StringBuilder(rawValue);
        int offSetInFinalAdjusted = finalAdjustedItem.length();
        while (finalAdjustedItem.length() < maskAsChar.length){
            String adjustementCharacter = maskAsChar[offSetInFinalAdjusted] == '9' ? "0":"Z";
            finalAdjustedItem.append(adjustementCharacter);
            offSetInFinalAdjusted++;
        }

        return finalAdjustedItem.toString();
    }

    private static String formatValueWithMask(String inputMask, String rawValue) {
		int offSetInFinalFormated = 0;
		StringBuilder finalFormattedItem = new StringBuilder(rawValue);
		char[] maskAsChar = inputMask.toCharArray();
		String maskItemAsString;
		for (char maskItem : maskAsChar) {
			maskItemAsString = String.valueOf(maskItem);

			if((maskItemAsString).matches(REGEX_STRICT_MASK_ITEMS))
			{
				finalFormattedItem.insert(offSetInFinalFormated, maskItemAsString);
			}

			offSetInFinalFormated++;
		}

		return finalFormattedItem.toString();
	}
}
