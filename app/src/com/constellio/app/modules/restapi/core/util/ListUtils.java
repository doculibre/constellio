package com.constellio.app.modules.restapi.core.util;

import com.google.common.collect.Iterables;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;

@UtilityClass
public final class ListUtils {

	public static <T> List<T> nullToEmpty(List<T> list) {
		if (list == null) {
			return Collections.emptyList();
		}
		return list;
	}

	public static <T> boolean isNullOrEmpty(List<T> list) {
		return list == null || list.isEmpty();
	}

	public static String[] asStringArray(List<String> list) {
		return Iterables.toArray(list, String.class);
	}

}
