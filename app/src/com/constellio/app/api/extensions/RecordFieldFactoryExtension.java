package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;

public class RecordFieldFactoryExtension {

	public RecordFieldFactory newRecordFieldFactory(RecordFieldFactoryExtensionParams params) {
		MetadataFieldFactory metadataFieldFactory = params.getMetadataFieldFactory();
		if (metadataFieldFactory == null) {
			metadataFieldFactory = new MetadataFieldFactory();
		}
		return new RecordFieldFactory(metadataFieldFactory);
	}

}
