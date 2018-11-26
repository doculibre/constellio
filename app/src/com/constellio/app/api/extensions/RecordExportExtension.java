package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.OnWriteRecordParams;

import java.util.List;

public abstract class RecordExportExtension {

	public void onWriteRecord(OnWriteRecordParams params) {

	}

	public List<String> getUnwantedTaxonomiesForExportation() {
		return null;
	}

}
