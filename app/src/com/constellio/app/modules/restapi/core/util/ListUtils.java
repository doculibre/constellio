package com.constellio.app.modules.restapi.core.util;

import com.google.common.collect.Iterables;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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


	public <T> List<T> flatMapFilteringNull(List<List<T>> sources) {
		return sources == null ? new ArrayList<>() : sources.stream()
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public <T> List<T> flatMapFilteringNull(List<T> firstSource, List<T>... otherSources) {
		List<List<T>> allSources = new ArrayList<>();
		if (firstSource != null) {
			allSources.add(firstSource);
		}

		if (otherSources != null) {
			allSources.addAll(Arrays.asList(otherSources));
		}

		return flatMapFilteringNull(allSources);
	}
}
