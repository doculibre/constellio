package com.constellio.sdk.tests.schemas;

import com.constellio.data.dao.services.DataStoreTypesFactory;

public class FakeDataStoreTypeFactory implements DataStoreTypesFactory {

	public static final String STRING = "string";
	public static final String STRINGS = "strings";

	public static final String TEXT = "text";
	public static final String TEXTS = "texts";

	public static final String DOUBLE = "double";
	public static final String DOUBLES = "doubles";

	public static final String DATE_TIME = "date";
	public static final String DATE_TIMES = "dates";

	public static final String DATE = "date";
	public static final String DATES = "dates";

	public static final String BOOLEAN = "boolean";
	public static final String BOOLEANS = "booleans";

	@Override
	public String forString(boolean multivalue) {
		return multivalue ? STRINGS : STRING;
	}

	@Override
	public String forText(boolean multivalue) {
		return multivalue ? TEXTS : TEXT;
	}

	@Override
	public String forDouble(boolean multivalue) {
		return multivalue ? DOUBLES : DOUBLE;
	}

	@Override
	public String forDateTime(boolean multivalue) {
		return multivalue ? DATE_TIMES : DATE_TIME;
	}

	@Override
	public String forDate(boolean multivalue) {
		return multivalue ? DATES : DATE;
	}

	@Override
	public String forBoolean(boolean multivalue) {
		return multivalue ? BOOLEANS : BOOLEAN;
	}

}
