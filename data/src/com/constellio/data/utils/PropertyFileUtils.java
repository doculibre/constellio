package com.constellio.data.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyFileUtils {

	public static Map<String, String> loadKeyValues(File propertyFiles) {
		Properties properties = loadPropertyFiles(propertyFiles);

		return loadPropertiesInAMap(properties);
	}

	public static void writeMap(File indexProperties, Map<String, String> map) {
		Properties properties = new Properties();
		properties.putAll(map);
		try (FileWriter fw = new FileWriter(indexProperties)) {
			properties.store(fw, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private static Map<String, String> loadPropertiesInAMap(Properties properties) {
		Map<String, String> configs = new LinkedHashMap<>();

		for (String name : properties.stringPropertyNames()) {
			configs.put(name, properties.getProperty(name));
		}
		return configs;
	}

	private static Properties loadPropertyFiles(File propertyFiles) {
		Properties properties = new Properties();

		Reader reader = null;
		try {
			reader = new FileReader(propertyFiles);
			properties.load(reader);

		} catch (IOException e) {
			throw new PropertyFileUtilsRuntimeException("Problem reading file '" + propertyFiles.getAbsolutePath() + "'", e);

		} finally {
			IOUtils.closeQuietly(reader);
		}
		return properties;
	}

	public static void store(Properties properties, OutputStream out)
			throws IOException {
		store0(properties, new BufferedWriter(new OutputStreamWriter(out, "8859_1")), true);
	}

	public static void store(Properties properties, File file)
			throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		store0(properties, new BufferedWriter(new OutputStreamWriter(out, "8859_1")), true);
	}

	private static void store0(Properties properties, BufferedWriter bw, boolean escUnicode)
			throws IOException {

		bw.newLine();
		synchronized (PropertyFileUtils.class) {
			for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
				String key = (String) e.nextElement();
				String val = (String) properties.get(key);
				key = saveConvert(key, true, escUnicode);
				/* No need to escape embedded and trailing spaces for value, hence
				 * pass false to flag.
				 */
				val = saveConvert(val, false, escUnicode);
				bw.write(key + "=" + val);
				bw.newLine();
			}
		}
		bw.flush();
	}

	/*
	 * Converts unicodes to encoded &#92;uxxxx and escapes
	 * special characters with a preceding slash
	 */
	private static String saveConvert(String theString,
									  boolean escapeSpace,
									  boolean escapeUnicode) {
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuffer outBuffer = new StringBuffer(bufLen);

		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch (aChar) {
				case ' ':
					if (x == 0 || escapeSpace) {
						outBuffer.append('\\');
					}
					outBuffer.append(' ');
					break;
				case '\t':
					outBuffer.append('\\');
					outBuffer.append('t');
					break;
				case '\n':
					outBuffer.append('\\');
					outBuffer.append('n');
					break;
				case '\r':
					outBuffer.append('\\');
					outBuffer.append('r');
					break;
				case '\f':
					outBuffer.append('\\');
					outBuffer.append('f');
					break;
				case '=': // Fall through
				case ':': // Fall through
				case '#': // Fall through
				case '!':
					outBuffer.append('\\');
					outBuffer.append(aChar);
					break;
				default:
					if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
						outBuffer.append('\\');
						outBuffer.append('u');
						outBuffer.append(toHex((aChar >> 12) & 0xF));
						outBuffer.append(toHex((aChar >> 8) & 0xF));
						outBuffer.append(toHex((aChar >> 4) & 0xF));
						outBuffer.append(toHex(aChar & 0xF));
					} else {
						outBuffer.append(aChar);
					}
			}
		}
		return outBuffer.toString();
	}

	/**
	 * Convert a nibble to a hex character
	 *
	 * @param nibble the nibble to convert.
	 */
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	/**
	 * A table of hex digits
	 */
	private static final char[] hexDigit = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

}
