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
package com.constellio.model.services.records.bulkImport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.constellio.model.services.records.bulkImport.data.ImportData;
import com.constellio.model.services.records.bulkImport.data.ImportDataIterator;
import com.constellio.model.services.records.bulkImport.data.ImportDataProvider;
import com.constellio.model.services.records.bulkImport.data.builder.ImportDataBuilder;

public class DummyImportDataProvider implements ImportDataProvider {

	private Map<String, List<ImportDataBuilder>> data;

	public DummyImportDataProvider(
			Map<String, List<ImportDataBuilder>> data) {
		this.data = data;
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}

	@Override
	public List<String> getAvailableSchemaTypes() {
		return new ArrayList<>(data.keySet());
	}

	@Override
	public ImportDataIterator newDataIterator(String schemaType) {
		final Iterator<ImportDataBuilder> nestedIterator = data.get(schemaType).iterator();
		return new ImportDataIterator() {

			private boolean closed;

			private int index;

			@Override
			public void close() {
				closed = true;
			}

			public boolean isClosed() {
				return closed;
			}

			@Override
			public boolean hasNext() {
				return nestedIterator.hasNext();
			}

			@Override
			public ImportData next() {
				return nestedIterator.next().build(index++);
			}

			@Override
			public void remove() {

			}
		};
	}
}
