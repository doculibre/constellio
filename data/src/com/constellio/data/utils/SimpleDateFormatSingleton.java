package com.constellio.data.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class SimpleDateFormatSingleton {

	private static ThreadLocal<Map<String, SimpleDateFormat>> dateFormats = new ThreadLocal<>();

	public static SimpleDateFormat getSimpleDateFormat(String pattern) {
		Map<String, SimpleDateFormat> patterns = dateFormats.get();
		if (patterns == null) {
			patterns = new HashMap<>();
			dateFormats.set(patterns);
		}
		SimpleDateFormat simpleDateFormat = patterns.get(pattern);
		if (simpleDateFormat == null) {
			simpleDateFormat = new SimpleDateFormat(pattern);
			patterns.put(pattern, simpleDateFormat);
		}
		return simpleDateFormat;

	}

}
