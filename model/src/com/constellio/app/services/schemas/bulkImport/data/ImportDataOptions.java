package com.constellio.app.services.schemas.bulkImport.data;

public class ImportDataOptions {

	private boolean mergeExistingRecordWithSameLegacyId = true;

	private boolean importAsLegacyId = true;
	private boolean synchronizeFilename = false;
	private boolean mergeExistingRecordWithSameUniqueMetadata = false;

	public boolean isImportAsLegacyId() {
		return importAsLegacyId;
	}

	public ImportDataOptions setImportAsLegacyId(boolean importAsLegacyId) {
		this.importAsLegacyId = importAsLegacyId;
		return this;
	}

	public boolean isSynchronizeFilename() {
		return synchronizeFilename;
	}

	public void setSynchronizeFilename(boolean synchronizeFilename) {
		this.synchronizeFilename = synchronizeFilename;
	}

	public boolean isMergeExistingRecordWithSameUniqueMetadata() {
		return mergeExistingRecordWithSameUniqueMetadata;
	}

	public ImportDataOptions setMergeExistingRecordWithSameUniqueMetadata(
			boolean mergeExistingRecordWithSameUniqueMetadata) {
		this.mergeExistingRecordWithSameUniqueMetadata = mergeExistingRecordWithSameUniqueMetadata;
		return this;
	}

	public boolean isMergeExistingRecordWithSameLegacyId() {
		return mergeExistingRecordWithSameLegacyId;
	}

	public ImportDataOptions setMergeExistingRecordWithSameLegacyId(boolean mergeExistingRecordWithSameLegacyId) {
		this.mergeExistingRecordWithSameLegacyId = mergeExistingRecordWithSameLegacyId;
		return this;
	}
}
