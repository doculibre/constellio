package com.constellio.app.services.importExport.records.writers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class ImportRecordOfSameCollectionWriter {

	File outputFolder;

	Map<String, ImportDataOptions> options = new HashMap<>();

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

		ImportRecordOfSameTypeWriter schemaTypeRecordsWriter = writers.get(importRecord.getSchemaType());
		if (schemaTypeRecordsWriter == null) {
			File typefile = new File(outputFolder, importRecord.getSchemaType() + ".xml");
			schemaTypeRecordsWriter = new ImportRecordOfSameTypeWriter(typefile);
			writers.put(importRecord.getSchemaType(), schemaTypeRecordsWriter);
		}

		schemaTypeRecordsWriter.write(importRecord);
	}

	public void setOptions(String schemaType, ImportDataOptions options) {
		ImportRecordOfSameTypeWriter schemaTypeRecordsWriter = writers.get(schemaType);
		if (schemaTypeRecordsWriter == null) {
			File typefile = new File(outputFolder, schemaType + ".xml");
			schemaTypeRecordsWriter = new ImportRecordOfSameTypeWriter(typefile);
			writers.put(schemaType, schemaTypeRecordsWriter);
			schemaTypeRecordsWriter.write(options);
		} else {
			throw new ImpossibleRuntimeException("Cannot set options twice or once a record has been writen");
		}

	}

	public void close() {
		for (ImportRecordOfSameTypeWriter writer : writers.values()) {
			writer.close();
		}
	}
}
