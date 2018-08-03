package com.constellio.app.modules.rm.ui.components.container.fields;

import com.constellio.app.ui.framework.data.trees.LinkableRecordTreeNodesDataProvider;

public class ContainerStorageTreeNodesDataProvider extends LinkableRecordTreeNodesDataProvider {

	public ContainerStorageTreeNodesDataProvider(String taxonomyCode, String schemaTypeCode, boolean writeAccess) {
		super(taxonomyCode, schemaTypeCode, writeAccess);
	}

}
