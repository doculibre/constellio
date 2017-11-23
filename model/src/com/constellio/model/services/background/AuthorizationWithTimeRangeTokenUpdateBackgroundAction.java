package com.constellio.model.services.background;

import static com.constellio.data.utils.TimeProvider.getLocalDate;
import static com.constellio.model.entities.records.RecordUpdateOptions.validationExceptionSafeOptions;
import static com.constellio.model.entities.records.TransactionRecordsReindexation.ALL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;

public class AuthorizationWithTimeRangeTokenUpdateBackgroundAction implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationWithTimeRangeTokenUpdateBackgroundAction.class);

	private ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private CollectionsListManager collectionsListManager;

	public AuthorizationWithTimeRangeTokenUpdateBackgroundAction(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
	}

	@Override
	public synchronized void run() {

		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
			for (SolrAuthorizationDetails auth : schemas.getAllAuthorizations()) {
				if (auth.hasModifiedStatusSinceLastTokenRecalculate()) {
					try {
						Transaction tx = new Transaction();
						tx.setOptions(validationExceptionSafeOptions().setForcedReindexationOfMetadatas(ALL()));
						tx.add(auth.setLastTokenRecalculate(getLocalDate()));
						tx.add(recordServices.getDocumentById(auth.getTarget()));

						recordServices.execute(tx);
					} catch (RecordServicesException e) {
						LOGGER.error("Error while recalculating tokens", e);
					}

				}
			}

		}

	}

	void executeTransaction(Transaction transaction) {
		try {
			recordServices.executeHandlingImpactsAsync(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}
}
