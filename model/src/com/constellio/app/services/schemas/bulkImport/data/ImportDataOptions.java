package com.constellio.app.services.schemas.bulkImport.data;

public class ImportDataOptions {

	private boolean importAsLegacyId = true;

	public boolean isImportAsLegacyId() {
		return importAsLegacyId;
	}

	public ImportDataOptions setImportAsLegacyId(boolean importAsLegacyId) {
		this.importAsLegacyId = importAsLegacyId;
		return this;
	}
}
