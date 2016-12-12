package com.constellio.app.services.schemas.bulkImport.data;

public class ImportDataOptions {

	private boolean importAsLegacyId = true;

	private boolean mergeExistingRecordWithSameUniqueMetadata = false;

	public boolean isImportAsLegacyId() {
		return importAsLegacyId;
	}

	public ImportDataOptions setImportAsLegacyId(boolean importAsLegacyId) {
		this.importAsLegacyId = importAsLegacyId;
		return this;
	}

	public boolean isMergeExistingRecordWithSameUniqueMetadata() {
		return mergeExistingRecordWithSameUniqueMetadata;
	}

	public ImportDataOptions setMergeExistingRecordWithSameUniqueMetadata(boolean mergeExistingRecordWithSameUniqueMetadata) {
		this.mergeExistingRecordWithSameUniqueMetadata = mergeExistingRecordWithSameUniqueMetadata;
		return this;
	}
}
