package com.constellio.model.services.records.utils;

import java.util.Comparator;
import java.util.function.Function;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;

public class RecordCodeComparator implements Comparator<Record> {

	@Override
	public int compare(Record o1, Record o2) {
		return compareRecords(o1, o2);
	}

	public static int compareRecords(Record o1, Record o2) {
		String code1 = o1.get(Schemas.CODE);
		String code2 = o2.get(Schemas.CODE);

		int result = compare(code1, code2);

		if (result == 0) {
			String title1 = o1.get(Schemas.CODE);
			String title2 = o2.get(Schemas.CODE);
			result = compare(title1, title2);
		}

		return result;
	}

	private static int compare(String s1, String s2) {
		if (s1 != null && s2 != null) {
			return s1.compareTo(s2);

		} else if (s1 != null) {
			return 1;

		} else if (s2 != null) {
			return -1;
		} else {
			return 0;
		}
	}
}
