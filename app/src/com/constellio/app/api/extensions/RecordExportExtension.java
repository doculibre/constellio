package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.OnWriteRecordParams;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class RecordExportExtension {

	public void onWriteRecord(OnWriteRecordParams params) {

	}

	public List<String> getUnwantedTaxonomiesForExportation() {
		return null;
	}

	public Set<String> getHashsToInclude() {
		return Collections.emptySet();
	}

}
