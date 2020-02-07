package com.constellio.model.services.background;

import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.records.RecordUpdateOptions.validationExceptionSafeOptions;
import static com.constellio.model.entities.records.TransactionRecordsReindexation.ALL;

public class RecordsReindexingBackgroundAction implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsReindexingBackgroundAction.class);

	private ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private CollectionsListManager collectionsListManager;

	public RecordsReindexingBackgroundAction(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
	}

	@Override
	public synchronized void run() {
		run(true);
	}

	public synchronized void run(boolean waitDuringOfficeHours) {

		boolean officeHours = new FoldersLocator().getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER
							  && TimeProvider.getLocalDateTime().getHourOfDay() >= 7
							  && TimeProvider.getLocalDateTime().getHourOfDay() <= 18;

		if (!Toggle.PERFORMANCE_TESTING.isEnabled()

			&& ReindexingServices.getReindexingInfos() == null
			&& (modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized() ||
				!modelLayerFactory.getConfiguration().isSummaryCacheEnabled())) {
			boolean found = false;
			for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
				LogicalSearchQuery query = new LogicalSearchQuery();
				query.setCondition(LogicalSearchQueryOperators.fromAllSchemasInExceptEvents(collection)
						.where(Schemas.MARKED_FOR_REINDEXING).isTrue());
				query.setNumberOfRows(100);
				query.setName("BackgroundThread:RecordsReindexingBackgroundAction:getMarkedForReindexing()");
				List<Record> records = searchServices.search(query);

				if (!records.isEmpty()) {
					Transaction transaction = new Transaction(records);
					transaction.setOptions(validationExceptionSafeOptions().setForcedReindexationOfMetadatas(ALL())
							.setOptimisticLockingResolution(EXCEPTION).setUpdateAggregatedMetadatas(true)
							.setOverwriteModificationDateAndUser(false));

					executeTransaction(transaction);
					found = true;
				}
			}

			if ((officeHours || !found)
				&& new FoldersLocator().getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}

	}

	private void executeTransaction(Transaction transaction) {
		try {
			recordServices.executeHandlingImpactsAsync(transaction);
		} catch (RecordServicesException e) {
			LOGGER.info("Optimistic locking while reindexing records", e);
			recordServices.flush();
		}
	}
}
