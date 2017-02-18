package com.constellio.app.modules.rm.ui.components.container.fields;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.model.services.taxonomies.FastContinueInfos;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

public class ContainerStorageTreeNodesDataProvider implements RecordTreeNodesDataProvider {

	@Override
	public LinkableTaxonomySearchResponse getChildrenNodes(String recordId, int start, int maxSize, FastContinueInfos infos) {

		TaxonomiesSearchServices services = ConstellioFactories.getInstance().getModelLayerFactory().newTaxonomiesSearchService();

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
