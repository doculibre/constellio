package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.ExportCollectionInfosSIPIsTaxonomySupportedParams;
import com.constellio.app.api.extensions.params.ExportCollectionInfosSIPParams;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

import java.io.IOException;

public class SIPExtension {

	public void exportCollectionInfosSIP(ExportCollectionInfosSIPParams params) throws IOException {

	}


	public ExtensionBooleanResult isExportedTaxonomyInSIPCollectionInfos(
			ExportCollectionInfosSIPIsTaxonomySupportedParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
