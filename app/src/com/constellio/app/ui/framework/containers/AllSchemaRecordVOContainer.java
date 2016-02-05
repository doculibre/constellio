package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.AllSchemaRecordVODataProvider;
import com.constellio.model.services.factories.ModelLayerFactory;

public class AllSchemaRecordVOContainer extends RecordVOLazyContainer {

	public AllSchemaRecordVOContainer(String schemaCode, String collection) {
		super(new AllSchemaRecordVODataProvider(schemaCode, collection));
	}

	public AllSchemaRecordVOContainer(MetadataSchemaVO schema, RecordToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory) {
		super(new AllSchemaRecordVODataProvider(schema, voBuilder, modelLayerFactory));
	}

}
