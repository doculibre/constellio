package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningServiceException.DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;
import org.apache.log4j.Logger;

public class DecommissioningAsyncTask implements AsyncTask {
	private static final Logger LOGGER = Logger.getLogger(DecommissioningService.class);

	private String collection;
	private String username;
	private String decommissioningListId;

	public DecommissioningAsyncTask(String collection, String username, String decommissioningListId) {
		this.collection = collection;
		this.username = username;
		this.decommissioningListId = decommissioningListId;
	}

	@Override
	public void execute(AsyncTaskExecutionParams params) {
		try {
			process(params, 0);
		} catch (Exception e) {
			// TODO::JOLA --> Add exception in a txt file.
		}
	}

	private void process(AsyncTaskExecutionParams params, int attempt) throws Exception {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		DecommissioningService decommissioningService = new DecommissioningService(collection, appLayerFactory);
		User user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(username, collection);
		DecommissioningList decommissioningList = rm.getDecommissioningList(decommissioningListId);
		Decommissioner decommissioner = Decommissioner.forList(decommissioningList, decommissioningService, appLayerFactory);

		int recordCount = 1;
		if (decommissioningList.getDecommissioningListType().isFolderList()) {
			recordCount += decommissioningList.getFolders().size();
			recordCount += decommissioningList.getContainers().size();
		} else {
			recordCount += decommissioningList.getDocuments().size();
		}

		if (attempt == 0) {
			params.setProgressionUpperLimit(recordCount + 1);
		}

		try {
			decommissioner.process(decommissioningList, user, TimeProvider.getLocalDate());
			params.incrementProgression(recordCount);
		} catch (RecordServicesException.OptimisticLocking e) {
			appLayerFactory.getModelLayerFactory().getRecordsCaches().getCache(decommissioningList.getCollection())
					.reloadSchemaType(Folder.SCHEMA_TYPE, true);

			appLayerFactory.getModelLayerFactory().getRecordsCaches().getCache(decommissioningList.getCollection())
					.reloadSchemaType(ContainerRecord.SCHEMA_TYPE, true);

			if (attempt < 3) {
				LOGGER.warn("Decommission failed, retrying...", e);
				process(params, attempt + 1);
			} else {
				throw new DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission();
			}
		}
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[]{collection, username, decommissioningListId};
	}
}
