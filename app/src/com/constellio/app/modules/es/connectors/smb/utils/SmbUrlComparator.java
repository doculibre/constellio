package com.constellio.app.modules.es.connectors.smb.utils;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

public class SmbUrlComparator implements Comparator<String> {
	// That there is a systematic order is more important than the actual order.
	@Override
	public int compare(String o1, String o2) {
		// case 1: smb://a smb://b
		// case 2: smb://a/b smb://a
		// case 3: smb://a smb://a/b
		// case 4: smb://a smb://a

		String[] array1 = StringUtils.split(o1, "/");
		String[] array2 = StringUtils.split(o2, "/");

		for (int i = 0; i < array1.length; i++) {
			String part1 = array1[i];
			if (i < array2.length) {
				String part2 = array2[i];
				// case 1
				int compare = part1.compareTo(part2);
				if (compare != 0) {
					return compare;
				}
			} else { // case 2
				return 1;
			}
		}

		// case 3
		if (array2.length > array1.length) {
			return -1;
		}

		// case 4
		return 0;

	}
}