package com.constellio.app.services.importExport.records.writers;

public interface ImportedRecordFilter {

	boolean isImported(ModifiableImportRecord importRecord);

}
