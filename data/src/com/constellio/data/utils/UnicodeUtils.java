package com.constellio.data.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class UnicodeUtils {
	
	public static boolean isUnicodeEscaped(String s) {
		return s.contains("\\u");
	}
	
	public static String unicodeEscape(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++){
		    char c = s.charAt(i);
		    sb.append(unicodeEscape(c));
		}
		return sb.toString();
	}

	//--------------------------------------------------------------------------
	/**
	 * Converts the string to the unicode format '\u0020'.
	 * 
	 * This format is the Java source code format.
	 *
	 * <pre>
	 *   UnicodeUtils.unicodeEscape(' ') = "\u0020"
	 *   UnicodeUtils.unicodeEscaped('A') = "\u0041"
	 * </pre>
	 * 
	 * @param ch  the character to convert
	 * @return the escaped unicode string
	 */
	public static String unicodeEscape(char ch) {
	    if (ch < 0x10) {
	        return "\\u000" + Integer.toHexString(ch);
	    } else if (ch < 0x100) {
	        return "\\u00" + Integer.toHexString(ch);
	    } else if (ch < 0x1000) {
	        return "\\u0" + Integer.toHexString(ch);
	    }
	    return "\\u" + Integer.toHexString(ch);
	}
	
	public static String unicodeUnescape(String s) {
		Properties p = new Properties();
		try {
			p.load(new StringReader("key=" + s));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return p.getProperty("key");
	}

}
