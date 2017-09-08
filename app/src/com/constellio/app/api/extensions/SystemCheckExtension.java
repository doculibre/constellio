package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.CollectionSystemCheckParams;
import com.constellio.app.api.extensions.params.TryRepairAutomaticValueParams;
import com.constellio.app.api.extensions.params.ValidateRecordsCheckParams;

public class SystemCheckExtension {

	public void checkCollection(CollectionSystemCheckParams collectionCheckParams) {

	}

	public boolean validateRecord(ValidateRecordsCheckParams validateRecordsCheckParams) {
		return false;
	}

	public boolean tryRepairAutomaticValue(TryRepairAutomaticValueParams params) {
		return false;
	}
}
