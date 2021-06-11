package com.constellio.app.modules.restapi.core.util;

import com.google.common.collect.Iterables;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.Set;

@UtilityClass
public final class SetUtils {

	public static <T> Set<T> nullToEmpty(Set<T> set) {
		if (set == null) {
			return Collections.emptySet();
		}
		return set;
	}

	public static <T> boolean isNullOrEmpty(Set<T> set) {
		return set == null || set.isEmpty();
	}

	public static String[] asStringArray(Set<String> set) {
		return Iterables.toArray(set, String.class);
	}

}
