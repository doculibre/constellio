package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.search.SearchServices;

import java.util.ArrayList;
import java.util.List;

public class RMContainerRecordExtension extends RecordExtension {
	private final RMSchemasRecordsServices rmSchema;
	private final SearchServices searchServices;
	private final ModelLayerFactory modelLayerFactory;

	private List<String> removedCartsIds;

	public RMContainerRecordExtension(String collection, AppLayerFactory appLayerFactory) {
		rmSchema = new RMSchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		removedCartsIds = new ArrayList<>();
	}

	@Override
	public void recordInModificationBeforeValidationAndAutomaticValuesCalculation(
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.isSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			ContainerRecord containerRecord = rmSchema.wrapContainerRecord(event.getRecord());
			deleteNonExistentFavoritesIds(containerRecord);
		}
	}

	private void deleteNonExistentFavoritesIds(ContainerRecord container) {
		List<String> removedIds = new ArrayList<>();
		RecordsCaches recordsCaches = modelLayerFactory.getRecordsCaches();
		for (String cartId : container.getFavoritesList()) {
			if (!removedCartsIds.contains(cartId)) {
				if (recordsCaches.getRecord(cartId) == null) {
					removedIds.add(cartId);
					removedCartsIds.add(cartId);
				}
			} else {
				removedIds.add(cartId);
			}
		}
		if (!removedIds.isEmpty()) {
			container.removeFavorites(removedIds);
		}
	}
}
