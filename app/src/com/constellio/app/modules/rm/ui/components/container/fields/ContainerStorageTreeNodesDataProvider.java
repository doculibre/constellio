package com.constellio.app.modules.rm.ui.components.container.fields;

import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.model.services.taxonomies.FastContinueInfos;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;

public class ContainerStorageTreeNodesDataProvider implements RecordTreeNodesDataProvider {
	@Override
	public LinkableTaxonomySearchResponse getChildrenNodes(String recordId, int start, int maxSize, FastContinueInfos infos) {
		return null;
	}

	@Override
	public LinkableTaxonomySearchResponse getRootNodes(int start, int maxSize, FastContinueInfos infos) {
		return null;
	}

	@Override
	public String getTaxonomyCode() {
		return null;
	}
}
