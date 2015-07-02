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
package com.constellio.app.services.schemas.bulkImport.data.excel;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorRuntimeException;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorTest;

public class ExcelFileImportDataIteratorAcceptanceTest extends ImportDataIteratorTest {

	ImportDataIterator importDataIterator;
	ExcelImportDataProvider excelImportDataProvider;

	@Before
	public void setUp() {
		excelImportDataProvider = new ExcelImportDataProvider(getTestResourceFile("data.xls"));
		excelImportDataProvider.initialize();
	}

	@Test
	public void whenIteratingThen()
			throws Exception {

		LocalDateTime localDateTime = new LocalDateTime(2010, 04, 29, 9, 31, 46, 235);
		LocalDate localDate = new LocalDate(2010, 05, 16);
		LocalDate anOtherLocalDate = new LocalDate(2010, 05, 15);

		importDataIterator = excelImportDataProvider.newDataIterator("datas");

		assertThat(importDataIterator.next()).has(id("1")).has(index(1)).has(schema("default"))
				.has(noField("id")).has(noField("schema"))
				.has(field("title", "Ze title"))
				.has(field("createdOn", localDateTime))
				.has(field("referenceToAnotherSchema", "42"))
				.has(field("zeEmptyField", null))
				.has(field("modifyOn", emptyList()));

		assertThat(importDataIterator.next()).has(id("42")).has(index(2)).has(schema("default"))
				.has(noField("id")).has(noField("schema"))
				.has(field("title", "Another title"))
				.has(field("referenceToAThirdSchema", "666"))
				.has(field("zeEmptyField", null))
				.has(field("modifyOn", emptyList()));

		assertThat(importDataIterator.next()).has(id("666")).has(index(3)).has(schema("customSchema"))
				.has(noField("id")).has(noField("schema"))
				.has(field("createdOn", localDateTime))
				.has(field("modifyOn", asList(anOtherLocalDate, localDate)))
				.has(field("keywords", asList("keyword1", "keyword2")))
				.has(field("zeNullField", null))
				.has(field("title", "A third title"));

	}

	@Test
	public void whenIteratingWithBlankLineThen()
			throws Exception {

		LocalDateTime localDateTime = new LocalDateTime(2010, 04, 29, 9, 31, 46, 235);
		LocalDate localDate = new LocalDate(2010, 05, 16);
		LocalDate anOtherLocalDate = new LocalDate(2010, 05, 15);

		importDataIterator = excelImportDataProvider.newDataIterator("content");

		assertThat(importDataIterator.next()).has(id("1")).has(index(1)).has(schema("default"))
				.has(noField("id")).has(noField("schema"))
				.has(field("title", "Ze title"))
				.has(field("createdOn", localDateTime))
				.has(field("referenceToAnotherSchema", "42"))
				.has(field("zeEmptyField", null));

		assertThat(importDataIterator.next()).has(id("666")).has(index(3)).has(schema("customSchema"))
				.has(noField("id")).has(noField("schema"))
				.has(field("createdOn", localDateTime))
				.has(field("modifyOn", asList(anOtherLocalDate, localDate)))
				.has(field("keywords", asList("keyword1", "keyword2")))
				.has(field("zeNullField", null))
				.has(field("title", "A third title"));

		assertThat(importDataIterator.next()).has(id("35")).has(index(6)).has(schema("default"))
				.has(noField("id")).has(noField("schema"))
				.has(field("createdOn", null))
				.has(field("modifyOn", emptyList()))
				.has(field("title", "There is also a title here"))
				.has(field("referenceToAThirdSchema", "42"));
	}

	@Test
	public void whenIteratingWithDateAndDateTimeCells()
			throws Exception {

		LocalDate localDate = new LocalDate(2010, 05, 16);
		LocalDate localDateFirst = new LocalDate(2010, 12, 28);
		LocalDate localDateSecond = new LocalDate(2015, 02, 25);
		LocalDate localDateThird = new LocalDate(2010, 04, 29);
		LocalDate anOtherLocalDate = new LocalDate(2010, 05, 15);
		LocalDateTime localDateTime = new LocalDateTime(2010, 04, 29, 9, 31, 46, 235);
		LocalDateTime localDateTimeSecond = new LocalDateTime(2015, 04, 12, 10, 31, 46, 265);

		importDataIterator = excelImportDataProvider.newDataIterator("dates");

		assertThat(importDataIterator.next()).has(id("1")).has(index(1)).has(schema("default"))
				.has(noField("id")).has(noField("schema"))
				.has(field("title", "Ze title"))
				.has(field("createdOn", localDateThird))
				.has(field("createdOnDateTime", localDateTimeSecond))
				.has(field("referenceToAnotherSchema", "42"))
				.has(field("zeEmptyField", null));

		assertThat(importDataIterator.next()).has(id("666")).has(index(3)).has(schema("customSchema"))
				.has(noField("id")).has(noField("schema"))
				.has(field("createdOn", localDateFirst))
				.has(field("createdOnDateTime", localDateTime))
				.has(field("modifyOn", asList(anOtherLocalDate, localDate)))
				.has(field("keywords", asList("keyword1", "keyword2")))
				.has(field("zeNullField", null))
				.has(field("title", "A third title"));

		assertThat(importDataIterator.next()).has(id("25")).has(index(5)).has(schema("anotherCustomSchema"))
				.has(noField("id")).has(noField("schema"))
				.has(field("createdOn", localDateSecond))
				.has(field("zeNullField", null))
				.has(field("title", "title"));

		assertThat(importDataIterator.next()).has(id("35")).has(index(6)).has(schema("default"))
				.has(noField("id")).has(noField("schema"))
				.has(field("createdOn", null))
				.has(field("zeNullField", null))
				.has(field("modifyOn", emptyList()))
				.has(field("title", "There is also a title here"))
				.has(field("referenceToAThirdSchema", "42"));
	}

	@Test(expected = ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate.class)
	public void givenWrongDateInfourthRecordThenExceptionExpected()
			throws Exception {

		importDataIterator = excelImportDataProvider.newDataIterator("datas");

		importDataIterator.next();
		importDataIterator.next();
		importDataIterator.next();

		//this one throw the exception :
		importDataIterator.next();

	}
/*
	//----------------------------------

	private Condition<? super ImportData> field(final String entryKey, final Object entryValue) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getFields()).containsEntry(entryKey, entryValue);
				return true;
			}
		};
	}

	private Condition<? super ImportData> noField(final String entryKey) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getFields()).doesNotContainKey(entryKey);
				return true;
			}
		};
	}

	private Condition<? super ImportData> schema(final String schema) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getSchema()).isEqualTo(schema);
				return true;
			}
		};
	}

	private Condition<? super ImportData> id(final String expectedId) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getLegacyId()).isEqualTo(expectedId);
				return true;
			}
		};
	}

	private Condition<? super ImportData> index(final int expectedIndex) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getIndex()).isEqualTo(expectedIndex);
				return true;
			}
		};
	}*/
}
