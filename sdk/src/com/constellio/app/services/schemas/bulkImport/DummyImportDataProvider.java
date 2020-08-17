package com.constellio.app.services.schemas.bulkImport;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.builder.ImportDataBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DummyImportDataProvider implements ImportDataProvider {

	public Map<String, ImportDataOptions> dataOptionsMap = new HashMap<>();

	private Map<String, List<ImportDataBuilder>> data;

	public DummyImportDataProvider(
			Map<String, List<ImportDataBuilder>> data) {
		this.data = data;
	}

	public DummyImportDataProvider() {
		this.data = new HashMap<>();
	}

	public void add(String schemaType, ImportDataBuilder record) {
		if (!data.containsKey(schemaType)) {
			data.put(schemaType, new ArrayList<ImportDataBuilder>());
		}
		data.get(schemaType).add(record);
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

	public ImportDataOptions getDataOptions(String schemaType) {
		if (dataOptionsMap.containsKey(schemaType)) {
			return dataOptionsMap.get(schemaType);
		} else {
			return new ImportDataOptions();
		}
	}

	@Override
	public ImportDataIterator newDataIterator(final String schemaType) {
		final Iterator<ImportDataBuilder> nestedIterator = data.get(schemaType).iterator();
		return new ImportDataIterator() {

			private boolean closed;

			private int index;

			@Override
			public ImportDataOptions getOptions() {
				return getDataOptions(schemaType);
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

	@Override
	public List<File> getImportedContents() {
		return null;
	}
}
