package com.constellio.model.services.records.utils;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.sort.DefaultStringSortFieldNormalizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class RecordCodeComparator implements Comparator<Supplier<Record>> {

	List<String> typesOrder;

	public RecordCodeComparator(List<String> typesOrder) {
		this.typesOrder = typesOrder;
	}

	@Override
	public int compare(Supplier<Record> o1, Supplier<Record> o2) {
		return compareRecords(o1, o2);
	}

	public static int compareRecords(Supplier<Record> o1, Supplier<Record> o2) {
		return compareRecords(new ArrayList<String>(), o1, o2);
	}

	public static int compareRecords(List<String> typesOrder, Supplier<Record> o1Supplier,
									 Supplier<Record> o2Supplier) {
		Record o1 = o1Supplier.get();
		Record o2 = o2Supplier.get();

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
