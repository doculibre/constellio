package com.constellio.app.modules.rm.reports.model.search;

import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchResultReportModel {
	private final List<List<Object>> results = new ArrayList<>();
	private final List<String> columnsTitles = new ArrayList<>();
	private ModelLayerFactory modelLayerFactory;

	public SearchResultReportModel(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public List<List<Object>> getResults() {
		return new ArrayList<>(CollectionUtils.unmodifiableCollection(results));
	}

	public List<String> getColumnsTitles() {
		return new ArrayList<>(CollectionUtils.unmodifiableCollection(columnsTitles));
	}

	public void addTitle(String title) {
		columnsTitles.add(title);
	}

	public void addLine(List<Object> recordLine) {
		results.add(recordLine);
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}
}
