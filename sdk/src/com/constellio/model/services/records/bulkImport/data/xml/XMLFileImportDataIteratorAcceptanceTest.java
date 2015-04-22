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
package com.constellio.model.services.records.bulkImport.data.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static java.util.Arrays.asList;

import java.io.Reader;

import org.assertj.core.api.Condition;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.records.bulkImport.data.ImportData;
import com.constellio.model.services.records.bulkImport.data.ImportDataIterator;
import com.constellio.model.services.records.bulkImport.data.ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate;
import com.constellio.sdk.tests.ConstellioTest;

public class XMLFileImportDataIteratorAcceptanceTest extends ConstellioTest {

	IOServices ioServices;

	ImportDataIterator importDataIterator;

	@Before
	public void setUp()
			throws Exception {
		ioServices = spy(getIOLayerFactory().newIOServices());

	}

	@Test
	public void whenIteratingThen()
			throws Exception {
		
		LocalDateTime localDateTime = new LocalDateTime(2010, 04, 29, 9, 31, 46, 235); 
		LocalDate localDate = new LocalDate(2010, 04, 29);
		LocalDate anOtherLocalDate = new LocalDate(2010, 05, 15);
		LocalDate aThirdLocalDate = new LocalDate(2010, 05, 16);
		

		importDataIterator = new XMLFileImportDataIterator(getTestResourceReader("data.xml"), ioServices);

		assertThat(importDataIterator.next()).has(id("1")).has(index(1)).has(schema("default"))
				.has(noField("id")).has(noField("schema"))
				.has(field("title", "Ze title"))
				.has(field("createdOn", localDateTime))
				.has(field("referenceToAnotherSchema", "42"));

		assertThat(importDataIterator.next()).has(id("42")).has(index(1)).has(schema("default"))
				.has(noField("id")).has(noField("schema"))
				.has(field("title", "Another title"))
				.has(field("referenceToAThirdSchema", "666"))
				.has(noField("zeEmptyField"));

		assertThat(importDataIterator.next()).has(id("666")).has(index(1)).has(schema("customSchema"))
				.has(noField("id")).has(noField("schema"))
				.has(field("createdOn", localDate))
				.has(field("modifyOn", asList(anOtherLocalDate, aThirdLocalDate)))
				.has(field("keywords", asList("keyword1", "keyword2")))
				.has(noField("zeNullField"))
				.has(field("title", "A third title"));
				
	}

	@Test (expected=ImportDataIteratorRuntimeException_InvalidDate.class) 
	public void givenWrongDateInfourthRecordThenExceptionExpected() 
			throws Exception {
		
		importDataIterator = new XMLFileImportDataIterator(getTestResourceReader("data.xml"), ioServices);
		
		importDataIterator.next();
		importDataIterator.next();
		importDataIterator.next();
		
		//this one throw the exception :
		importDataIterator.next();
		
	}

	@Test
	public void whenClosingIteratorThenReaderClosed()
			throws Exception {

		Reader reader = getTestResourceReader("data.xml");
		importDataIterator = new XMLFileImportDataIterator(reader, ioServices);

		importDataIterator.next();
		importDataIterator.close();

		verify(ioServices).closeQuietly(reader);
	}

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
	}
}
