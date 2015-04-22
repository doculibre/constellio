/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
