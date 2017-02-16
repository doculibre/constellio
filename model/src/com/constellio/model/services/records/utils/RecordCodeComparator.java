package com.constellio.model.services.records.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.sort.DefaultStringSortFieldNormalizer;
import com.constellio.model.entities.schemas.sort.StringSortFieldNormalizer;

public class RecordCodeComparator implements Comparator<Record> {

	List<String> typesOrder;

	public RecordCodeComparator(List<String> typesOrder) {
		this.typesOrder = typesOrder;
	}

	@Override
	public int compare(Record o1, Record o2) {
		return compareRecords(o1, o2);
	}

	public static int compareRecords(Record o1, Record o2) {
		return compareRecords(new ArrayList<String>(), o1, o2);
	}

	public static int compareRecords(List<String> typesOrder, Record o1, Record o2) {

		int type1 = typesOrder.indexOf(o1.getTypeCode());
		int type2 = typesOrder.indexOf(o2.getTypeCode());

		if (type1 == -1) {
			type1 = 10000;
		}
		if (type2 == -1) {
			type2 = 10000;
		}

		int result = new Integer(type1).compareTo(type2);
		if (result == 0) {

			String code1 = convert(o1.<String>get(Schemas.CODE));
			String code2 = convert(o2.<String>get(Schemas.CODE));

			result = compare(code1, code2);

			if (result == 0) {
				String title1 = convert(o1.<String>get(Schemas.TITLE));
				String title2 = convert(o2.<String>get(Schemas.TITLE));
				result = compare(title1, title2);
			}
		}

		return result;
	}

	static DefaultStringSortFieldNormalizer normalizer = new DefaultStringSortFieldNormalizer();

	private static String convert(String value) {

		if (value == null) {
			return normalizer.normalizeNull();
		} else {
			return normalizer.normalize(value);
		}
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
