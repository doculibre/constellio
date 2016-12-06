package com.constellio.app.services.importExport.records.writers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImportRecordOfSameCollectionWriter {

	File outputFolder;

	Map<String, ImportRecordOfSameTypeWriter> writers = new HashMap<>();

	public ImportRecordOfSameCollectionWriter(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void write(ModifiableImportRecord importRecord) {
		if (importRecord.getCollection() == null) {
			throw new RuntimeException("Collection is required!");
		}

		if (importRecord.getSchemaType() == null) {
			throw new RuntimeException("Schema type is required!");
		}

		ImportRecordOfSameTypeWriter collectionWriter = writers.get(importRecord.getSchemaType());
		if (collectionWriter == null) {
			File typefile = new File(outputFolder, importRecord.getSchemaType() + ".xml");
			collectionWriter = new ImportRecordOfSameTypeWriter(typefile);
			writers.put(importRecord.getSchemaType(), collectionWriter);
		}

		collectionWriter.write(importRecord);
	}

	public void close() {
		for (ImportRecordOfSameTypeWriter writer : writers.values()) {
			writer.close();
		}
	}
}
