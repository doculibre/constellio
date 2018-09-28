package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.collections.ListUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMContainerRecordExtension extends RecordExtension {
	private final RMSchemasRecordsServices rmSchema;
	private final SearchServices searchServices;

	public RMContainerRecordExtension(String collection, AppLayerFactory appLayerFactory) {
		rmSchema = new RMSchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
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
		for (String cartId : oldFavoritesList) {
			LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(rmSchema.cart.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(cartId));
			if (searchServices.getResultsCount(logicalSearchQuery) == 0) {
				removedIds.add(cartId);
			}
		}
		List<String> newFavoritesList = ListUtils.subtract(oldFavoritesList, removedIds);
		containerRecord.setFavoritesList(newFavoritesList);
	}
}
