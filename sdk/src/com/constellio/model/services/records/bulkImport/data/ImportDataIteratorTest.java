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
package com.constellio.model.services.records.bulkImport.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Condition;

import com.constellio.model.services.records.ContentImport;
import com.constellio.model.services.records.ContentImportVersion;
import com.constellio.sdk.tests.ConstellioTest;

public class ImportDataIteratorTest extends ConstellioTest {

	protected Condition<? super ImportData> noField(final String entryKey) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getFields()).doesNotContainKey(entryKey);
				return true;
			}
		};
	}

	protected Condition<? super ImportData> schema(final String schema) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getSchema()).isEqualTo(schema);
				return true;
			}
		};
	}

	protected Condition<? super ImportData> id(final String expectedId) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getLegacyId()).isEqualTo(expectedId);
				return true;
			}
		};
	}

	protected Condition<? super ImportData> index(final int expectedIndex) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getIndex()).isEqualTo(expectedIndex);
				return true;
			}
		};
	}


	protected Condition<? super ImportData> field(final String entryKey, final Object entryValue) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat(value.getFields()).containsEntry(entryKey, entryValue);
				return true;
			}
		};
	}

	protected Condition<? super ImportData> structure(final String entryKey, final Object entryListMap) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				boolean hasValue = false;
				assertThat(value.getFields().containsKey(entryKey));
				for (Object realValues : (List) value.getValue(entryKey)) {
					for (Object expectedValues : (List) entryListMap) {
						if (realValues.equals(expectedValues)) {
							hasValue = true;
						}
					}
					assertThat(hasValue).isTrue();
					hasValue = false;
				}

				for (Object expectedValues : (List) entryListMap) {
					for (Object realValues : (List) value.getValue(entryKey)) {
						if (expectedValues.equals(realValues)) {
							hasValue = true;
						}
					}
					assertThat(hasValue).isTrue();
					hasValue = false;
				}
				return true;
			}
		};
	}

	protected Condition<? super ImportData> contentSize(final int sizeExpected) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				assertThat((((ContentImport) value.getValue("content")).getVersions()).size()).isEqualTo(sizeExpected);
				return true;
			}
		};
	}

	protected Condition<? super ImportData> content(final ContentImportVersion... versions) {
		return new Condition<ImportData>() {
			@Override
			public boolean matches(ImportData value) {
				int index = 0;
				for (ContentImportVersion version : (((ContentImport) value.getValue("content")).getVersions())) {
					assertThat((version.getFileName())).isEqualTo(versions[index].getFileName());
					assertThat((version.getUrl())).isEqualTo(versions[index].getUrl());
					assertThat((version.isMajor())).isEqualTo(versions[index].isMajor());
					index++;
				}
				return true;
			}
		};
	}
}
