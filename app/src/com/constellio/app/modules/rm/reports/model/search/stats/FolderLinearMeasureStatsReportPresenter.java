package com.constellio.app.modules.rm.reports.model.search.stats;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

public class FolderLinearMeasureStatsReportPresenter {
	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemaTypes types;
	private TaxonomiesSearchOptions searchOptions;
	private TaxonomiesSearchServices searchService;
	private RMSchemasRecordsServices rmSchemasRecordsServices;

	public FolderLinearMeasureStatsReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
	}

	public FolderLinearMeasureStatsReportPresenter(String collection, ModelLayerFactory modelLayerFactory,
												   boolean withUsers) {

		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
	}

	public StatsReportModel build() {
		init();
		return null;
	}

	private void init() {
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all());
		searchService = modelLayerFactory.newTaxonomiesSearchService();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}
}