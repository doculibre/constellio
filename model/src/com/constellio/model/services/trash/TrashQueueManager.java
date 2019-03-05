package com.constellio.model.services.trash;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TrashQueueManager implements StatefulService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TrashQueueManager.class);

	private final ModelLayerFactory modelLayerfactory;
	private final BackgroundThreadsManager backgroundThreadsManager;
	private final SearchServices searchServices;

	public TrashQueueManager(ModelLayerFactory modelLayerfactory) {
		this.modelLayerfactory = modelLayerfactory;
		this.backgroundThreadsManager = this.modelLayerfactory.getDataLayerFactory().getBackgroundThreadsManager();
		this.searchServices = modelLayerfactory.newSearchServices();
	}

	@Override
	public void initialize() {
		configureBackgroundThread();
	}

	private void configureBackgroundThread() {

		Runnable deleteTrashRecordsAction = new Runnable() {
			@Override
			public void run() {
				try {
					if(! (boolean) modelLayerfactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.IS_TRASH_THREAD_EXECUTING)) {
						return;
					}
					deleteTrashRecords();
				} catch (Throwable e) {
					LOGGER.error("Exception when deleting records ", e);
				}
			}
		};

		backgroundThreadsManager.configure(BackgroundThreadConfiguration
				.repeatingAction("TrashQueueManager", deleteTrashRecordsAction)
				.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE)
				.executedEvery(Duration.standardHours(3))
				.between(new LocalTime(21, 0, 0), new LocalTime(4, 0, 0)));
	}

	public void deleteTrashRecords() {
		LocalDateTime now = TimeProvider.getLocalDateTime();
		Integer keepLogicalRecordsDurationInDays = modelLayerfactory.getSystemConfigs().getTrashPurgeDelai();
		LocalDateTime recordsToDeleteLogicallDeleteStartDate = now.minusDays(keepLogicalRecordsDurationInDays);

		List<String> collections = modelLayerfactory.getCollectionsListManager().getCollectionsExcludingSystem();
		for (String collection : collections) {
			TrashServices trashServices = new TrashServices(modelLayerfactory, collection);
			LogicalSearchQuery query = trashServices
					.getTrashRecordsQueryForCollectionDeletedBeforeDate(collection, recordsToDeleteLogicallDeleteStartDate);
			LOGGER.info("Remaining " + searchServices.getResultsCount(query) + " records to delete in collection " + collection);
			SearchResponseIterator<Record> it = searchServices.recordsIterator(query);
			while (it.hasNext()) {
				try {
					Record recordToDelete = it.next();
					trashServices.handleRecordPhysicalDelete(recordToDelete, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void close() {

	}
}
