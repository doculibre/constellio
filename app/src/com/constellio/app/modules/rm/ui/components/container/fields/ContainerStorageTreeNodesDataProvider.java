package com.constellio.app.modules.rm.ui.components.container.fields;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.trees.LinkableRecordTreeNodesDataProvider;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.model.services.taxonomies.*;

public class ContainerStorageTreeNodesDataProvider extends LinkableRecordTreeNodesDataProvider {

	public ContainerStorageTreeNodesDataProvider(String taxonomyCode, String schemaTypeCode, boolean writeAccess) {
		super(taxonomyCode, schemaTypeCode, writeAccess);
	}

}
