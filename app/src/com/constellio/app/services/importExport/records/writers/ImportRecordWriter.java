package com.constellio.app.services.importExport.records.writers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImportRecordWriter {

	File outputFolder;

	Map<String, ImportRecordOfSameCollectionWriter> writers = new HashMap<>();

	ImportedRecordFilter filter;

	public ImportRecordWriter(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public ImportRecordWriter(File outputFolder, ImportedRecordFilter filter) {
		this.outputFolder = outputFolder;
		this.filter = filter;
	}

	public void write(ModifiableImportRecord importRecord) {

		if (importRecord.getCollection() == null) {
			throw new RuntimeException("Collection is required!");
		}

		if (importRecord.getSchemaType() == null) {
			throw new RuntimeException("Schema type is required!");
		}

		if (filter == null || filter.isImported(importRecord)) {
			ImportRecordOfSameCollectionWriter collectionWriter = writers.get(importRecord.getCollection());
			if (collectionWriter == null) {
				File collectionFolder = new File(outputFolder, importRecord.getCollection());
				collectionFolder.mkdirs();
				collectionWriter = new ImportRecordOfSameCollectionWriter(collectionFolder);
				writers.put(importRecord.getCollection(), collectionWriter);
			}

			collectionWriter.write(importRecord);
		}
	}

	public void close() {
		for (ImportRecordOfSameCollectionWriter writer : writers.values()) {
			writer.close();
		}
	}

}
