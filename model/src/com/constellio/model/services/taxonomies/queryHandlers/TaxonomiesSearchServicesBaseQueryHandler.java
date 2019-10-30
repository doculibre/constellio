package com.constellio.model.services.taxonomies.queryHandlers;

import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.LogicalSearchQueryExecutorInCache;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesCache;

public class TaxonomiesSearchServicesBaseQueryHandler {

	//private static final String CHILDREN_QUERY = "children";
	protected static final boolean NOT_LINKABLE = false;

	protected SearchServices searchServices;
	protected TaxonomiesManager taxonomiesManager;
	protected MetadataSchemasManager metadataSchemasManager;
	protected RecordServices recordServices;
	protected SchemaUtils schemaUtils = new SchemaUtils();
	protected ConceptNodesTaxonomySearchServices conceptNodesTaxonomySearchServices;
	protected RecordsCaches caches;
	protected TaxonomiesSearchServicesCache cache;
	protected ModelLayerExtensions extensions;
	protected ModelLayerFactory modelLayerFactory;
	protected LogicalSearchQueryExecutorInCache queryExecutorInCache;

	public TaxonomiesSearchServicesBaseQueryHandler(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.conceptNodesTaxonomySearchServices = new ConceptNodesTaxonomySearchServices(modelLayerFactory);
		this.caches = modelLayerFactory.getRecordsCaches();
		this.cache = modelLayerFactory.getTaxonomiesSearchServicesCache();
		this.extensions = modelLayerFactory.getExtensions();
		this.queryExecutorInCache = searchServices.getQueryExecutorInCache();
	}


}
