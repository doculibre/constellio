package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningServiceException.DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class DecommissioningAsyncTask implements AsyncTask {
	private static final Logger LOGGER = Logger.getLogger(DecommissioningService.class);

	private AppLayerFactory appLayerFactory;

	private String collection;
	private String username;
	private String decommissioningListId;

	public DecommissioningAsyncTask(String collection, String username, String decommissioningListId) {
		this.collection = collection;
		this.username = username;
		this.decommissioningListId = decommissioningListId;

		appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[]{collection, username, decommissioningListId};
	}

	@Override
	public void execute(AsyncTaskExecutionParams params) {
		try {
			process(params, 0);
		} catch (Exception e) {
			writeErrorToReport(params, MessageUtils.toMessage(e));
		}
	}

	private void process(AsyncTaskExecutionParams params, int attempt) throws Exception {
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
			params.setProgressionUpperLimit(recordCount);
		}

		try {
			decommissioner.process(decommissioningList, user, TimeProvider.getLocalDate());
			params.incrementProgression(recordCount);
		} catch (RecordServicesException.OptimisticLocking e) {
			// TODO --> Make sure to remove these 2 reload when the optimistic locking version issue will be fixed.
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

	private void writeErrorToReport(AsyncTaskExecutionParams params, String message) {
		ContentManager contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		User user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(username, collection);

		File txtReport = null;
		OutputStream txtOuputStream = null;
		try {
			txtReport = new File(new FoldersLocator().getWorkFolder(),
					params.getBatchProcess().getId() + File.separator + "batchProcessReport.txt");
			txtOuputStream = FileUtils.openOutputStream(txtReport, true);

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(txtOuputStream));
			writer.write(message);
			writer.flush();

			InputStream txtInputStream = new FileInputStream(txtReport);
			ContentVersionDataSummary contentVersion = contentManager.upload(txtInputStream, txtReport.getName()).getContentVersionDataSummary();
			Content content = contentManager.createMajor(user, txtReport.getName(), contentVersion);

			BatchProcessReport report = getLinkedBatchProcessReport(params.getBatchProcess(), appLayerFactory);
			report.setContent(content);
			updateBatchProcessReport(report);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(txtOuputStream);
			FileUtils.deleteQuietly(txtReport);
		}
	}

	private void updateBatchProcessReport(BatchProcessReport report) {
		try {
			Transaction transaction = new Transaction();
			transaction.addUpdate(report.getWrappedRecord());
			appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BatchProcessReport getLinkedBatchProcessReport(BatchProcess batchProcess, AppLayerFactory appLayerFactory) {
		BatchProcessReport report = null;
		String collection = batchProcess.getCollection();
		if (collection != null) {
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
			User user = modelLayerFactory.newUserServices().getUserRecordInCollection(batchProcess.getUsername(), collection);
			String userId = user != null ? user.getId() : null;
			try {
				MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
				MetadataSchema batchProcessReportSchema = schemasManager.getSchemaTypes(collection)
						.getSchema(BatchProcessReport.FULL_SCHEMA);
				Record reportRecord = modelLayerFactory.newSearchServices().searchSingleResult(from(batchProcessReportSchema)
						.where(batchProcessReportSchema.getMetadata(BatchProcessReport.LINKED_BATCH_PROCESS))
						.isEqualTo(batchProcess.getId()));
				if (reportRecord != null) {
					report = new BatchProcessReport(reportRecord, schemasManager.getSchemaTypes(collection));
				} else {
					report = schemas.newBatchProcessReport();
					report.setLinkedBatchProcess(batchProcess.getId());
					report.setCreatedBy(userId);
				}
			} catch (Exception e) {
				report = schemas.newBatchProcessReport();
				report.setLinkedBatchProcess(batchProcess.getId());
				report.setCreatedBy(userId);
			}
		}
		return report;
	}
}
