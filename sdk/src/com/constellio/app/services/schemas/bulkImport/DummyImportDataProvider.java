package com.constellio.app.services.schemas.bulkImport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.builder.ImportDataBuilder;

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
	public int size(String schemaType) {
		return data.get(schemaType).size();
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
			public ImportDataOptions getOptions() {
				return new ImportDataOptions();
			}

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
