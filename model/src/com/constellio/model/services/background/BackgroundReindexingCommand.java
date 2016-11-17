package com.constellio.model.services.background;

import static com.constellio.model.entities.records.TransactionRecordsReindexation.ALL;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

public class BackgroundReindexingCommand implements Runnable {

	private ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private CollectionsListManager collectionsListManager;

	public BackgroundReindexingCommand(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
	}

	@Override
	public void run() {

		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			LogicalSearchQuery query = new LogicalSearchQuery();
			query.setCondition(LogicalSearchQueryOperators.fromAllSchemasInExceptEvents(collection)
					.where(Schemas.MARKED_FOR_REINDEXING).isTrue());
			query.setNumberOfRows(1000);

			List<Record> records = searchServices.search(query);
			Transaction transaction = new Transaction(records);
			transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(ALL());
			try {
				recordServices.executeHandlingImpactsAsync(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}

	}
}
