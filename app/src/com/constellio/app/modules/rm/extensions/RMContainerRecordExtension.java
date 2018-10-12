package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.search.SearchServices;
import org.apache.commons.collections.ListUtils;

import java.util.ArrayList;
import java.util.List;

public class RMContainerRecordExtension extends RecordExtension {
	private final RMSchemasRecordsServices rmSchema;
	private final SearchServices searchServices;
	private final ModelLayerFactory modelLayerFactory;

	public RMContainerRecordExtension(String collection, AppLayerFactory appLayerFactory) {
		rmSchema = new RMSchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
	}

	@Override
	public void recordInModificationBeforeValidationAndAutomaticValuesCalculation(
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.isSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			ContainerRecord containerRecord = rmSchema.wrapContainerRecord(event.getRecord());
			deleteNonExistentFavoritesIds(containerRecord);
		}
	}

	private void deleteNonExistentFavoritesIds(ContainerRecord containerRecord) {
		List<String> oldFavoritesList = containerRecord.getFavoritesList();
		List<String> removedIds = new ArrayList<>();
		RecordsCaches recordsCaches = modelLayerFactory.getRecordsCaches();
		for (String cartId : oldFavoritesList) {
			if (recordsCaches.getRecord(cartId) == null) {
				removedIds.add(cartId);
			}
		}
		List<String> newFavoritesList = ListUtils.subtract(oldFavoritesList, removedIds);
		containerRecord.setFavoritesList(newFavoritesList);
	}
}
