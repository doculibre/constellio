package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookRetriever;

import java.util.HashMap;
import java.util.Map;

public class HookTaxonomiesSearchServicesCache implements TaxonomiesSearchServicesCache {

	Map<String, TaxonomyRecordsHookRetriever> collectionRetrievers = new HashMap<>();

	ModelLayerFactory modelLayerFactory;

	public HookTaxonomiesSearchServicesCache(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	@Override
	public void initialize(String collection) {

	}

	@Override
	public synchronized void insert(String username, String recordId, String mode, Boolean value) {
		//Nothing to do, cache is automatically handled by backend
	}

	@Override
	public synchronized void invalidateAll() {
		//Nothing to do, cache is automatically handled by backend
	}

	@Override
	public synchronized void invalidateWithChildren(String recordId) {
		//Nothing to do, cache is automatically handled by backend
	}

	@Override
	public synchronized void invalidateWithoutChildren(String recordId) {
		//Nothing to do, cache is automatically handled by backend
	}

	@Override
	public synchronized void invalidateRecord(String recordId) {
		//Nothing to do, cache is automatically handled by backend
	}

	@Override
	public synchronized void invalidateUser(String username) {
		//Nothing to do, cache is automatically handled by backend
	}

	@Override
	public synchronized Boolean getCachedValue(String username, Record record, String mode) {
		TaxonomyRecordsHookRetriever retriever = collectionRetrievers.get(record.getCollection());

		User user = modelLayerFactory.newUserServices().getUserInCollection(username, record.getCollection());

		MetadataSchemaType schemaType = modelLayerFactory.getMetadataSchemasManager().getSchemaTypeOf(record);
		if (modelLayerFactory.getTaxonomiesManager().isTypeInPrincipalTaxonomy(schemaType)) {
			if (mode.equals("visible")) {
				return retriever.hasUserAccessToSomethingInPrincipalConcept(user, record, false, false);
			}

		} else



		return null;

		return false;
	}

}
