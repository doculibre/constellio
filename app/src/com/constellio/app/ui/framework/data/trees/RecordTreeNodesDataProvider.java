package com.constellio.app.ui.framework.data.trees;

import org.apache.poi.ss.formula.functions.T;

import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;

public interface RecordTreeNodesDataProvider {

	LinkableTaxonomySearchResponse getChildrenNodes(String recordId, int start, int maxSize);

	LinkableTaxonomySearchResponse getRootNodes(int start, int maxSize);

	String getTaxonomyCode();
}
