package com.constellio.model.services.background;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.data.utils.TimeProvider.getLocalDate;
import static com.constellio.model.entities.records.RecordUpdateOptions.validationExceptionSafeOptions;
import static com.constellio.model.entities.records.TransactionRecordsReindexation.ALL;
import static com.constellio.model.entities.schemas.Schemas.ALL_REFERENCES;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

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
			List<Authorization> auths = schemas.getAllAuthorizationsInUnmodifiableState();
			for (Authorization auth : auths) {
				if (auth.hasModifiedStatusSinceLastTokenRecalculate()) {
					auth = auth.getCopyOfOriginalRecord();
					try {
						Transaction tx = new Transaction();
						tx.setOptions(validationExceptionSafeOptions().setForcedReindexationOfMetadatas(ALL()));
						tx.add(auth.setLastTokenRecalculate(getLocalDate()));

						Record target = recordServices.getDocumentById(auth.getTarget());
						MetadataSchemaType schemaType = modelLayerFactory.getMetadataSchemasManager().getSchemaTypeOf(target);

						if (schemaType.hasSecurity()) {
							tx.add(target);

						} else {
							//TODO i18n
							modelLayerFactory.getBatchProcessesManager().addPendingBatchProcess(
									fromAllSchemasIn(collection).where(ALL_REFERENCES).isEqualTo(target),
									ReindexMetadatasBatchProcessAction.allMetadatas(),
									"Update tokens"
							);
						}

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
