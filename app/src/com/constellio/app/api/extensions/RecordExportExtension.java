package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.ConvertStructureToMapParams;
import com.constellio.app.api.extensions.params.OnWriteRecordParams;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;

public abstract class RecordExportExtension {

	public Map<String, Object> convertStructureToMap(ConvertStructureToMapParams params) {
		return null;
	}

	public void onWriteRecord(OnWriteRecordParams params) {

	}

	public List<String> getUnwantedTaxonomiesForExportation() {
		return null;
	}

	public Set<String> getHashsToInclude() {
		return Collections.emptySet();
	}

}
