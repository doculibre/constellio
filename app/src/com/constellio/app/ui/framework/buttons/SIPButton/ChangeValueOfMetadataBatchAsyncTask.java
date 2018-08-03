package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.batchprocess.*;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.batch.controller.BatchProcessTask;
import com.constellio.model.services.batch.controller.RecordFromIdListIterator;
import com.constellio.model.services.batch.controller.TaskList;
import com.constellio.model.services.batch.state.BatchProcessProgressionServices;
import com.constellio.model.services.batch.state.InMemoryBatchProcessProgressionServices;
import com.constellio.model.services.batch.state.StoredBatchProcessPart;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.iterators.RecordSearchResponseIterator;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinPool;

import static com.constellio.model.services.records.RecordUtils.changeSchemaTypeAccordingToTypeLinkedSchema;

public class ChangeValueOfMetadataBatchAsyncTask implements AsyncTask {
	final Map<String, Object> metadataChangedValues;
	final String query;
	List<String> recordIds;
	final int numberOfExecutedRecords = 0;
//	private String id;
//	private BatchProcessStatus status;
//	private LocalDateTime requestDateTime;
//	private LocalDateTime startDateTime;
//	private int handledRecordsCount;
//	private int totalRecordsCount;
//	private int errors;
//	private BatchProcessAction action;
//	private String username;
//	private String title;
//	private String collection;
//	private String query;
//	private List<String> records;

	public ChangeValueOfMetadataBatchAsyncTask(Map<String, Object> metadataChangedValues, String query, List<String> recordIds) {
		this.metadataChangedValues = new HashMap<>(metadataChangedValues);
		this.query = query;
		this.recordIds = recordIds;
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[] { metadataChangedValues, query, recordIds };
	}

	@Override
	public void execute(AsyncTaskExecutionParams params) throws ValidationException {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		AsyncTaskBatchProcess batchProcess = params.getBatchProcess();
		BatchProcessReport report = getLinkedBatchProcessReport(batchProcess, appLayerFactory);
		int numberOfRecordsPerTask = 100;

		BatchProcessProgressionServices batchProcessProgressionServices = new InMemoryBatchProcessProgressionServices();
		StoredBatchProcessPart previousPart = batchProcessProgressionServices.getLastBatchProcessPart(batchProcess);
		BatchBuilderIterator<Record> batchIterator = getBatchIterator(appLayerFactory, previousPart);




		MetadataSchemaTypes schemaTypes = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(params.getCollection());
		RecordProvider recordProvider = new RecordProvider(appLayerFactory.getModelLayerFactory().newRecordServices());
		List<String> recordsWithErrors = new ArrayList<>();

		while (batchIterator.hasNext()) {
//			int oldErrorCount = recordsWithErrors.size();
//			List<String> newErrors = new ArrayList<>();
//			List<Record> records = batchIterator.next();
//			int index = previousPart == null ? 0 : previousPart.getIndex() + 1;
//			String firstId = records.get(0).getId();
//			String lastId = records.get(records.size() - 1).getId();
//			StoredBatchProcessPart storedBatchProcessPart = new StoredBatchProcessPart(batchProcess.getId(), index, firstId,
//					lastId, false, false);
//
//			//System.out.println("processing batch #" + index + " [" + firstId + "-" + lastId + "]");
//			batchProcessProgressionServices.markNewPartAsStarted(storedBatchProcessPart);
//			Transaction transaction = executeBatch(records, schemaTypes, recordProvider);
//
//			for (BatchProcessTask task : tasks) {
//				newErrors = pool.invoke(task);
//				recordsWithErrors.addAll(newErrors);
//			}
//
//			batchProcessProgressionServices.markPartAsFinished(storedBatchProcessPart);
//			previousPart = storedBatchProcessPart;
//			report.addSkippedRecords(newErrors);
//			updateBatchProcessReport(report, appLayerFactory);
//			if (batchIterator.hasNext()) {
//				batchProcessesManager.updateProgression(batchProcess, records.size(), recordsWithErrors.size() - oldErrorCount);
//			}
		}
	}

	private BatchBuilderIterator<Record> getBatchIterator(AppLayerFactory appLayerFactory, StoredBatchProcessPart previousPart) {
		Iterator iterator;

		if(recordIds == null) {
			ModifiableSolrParams params = new ModifiableSolrParams();
			try {
				params = SolrUtils.parseQueryString(query);
			} catch (Exception e) {
				e.printStackTrace();
			}
			params.set("sort", "principalPath_s asc, id asc");

			iterator = new RecordSearchResponseIterator(appLayerFactory.getModelLayerFactory(), params, 1000, true);
			if (previousPart != null) {
				((RecordSearchResponseIterator) iterator).beginAfterId(previousPart.getLastId());
			}
		} else {
			iterator = new RecordFromIdListIterator(recordIds, appLayerFactory.getModelLayerFactory());
			if (previousPart != null) {
				((RecordFromIdListIterator) iterator).beginAfterId(previousPart.getLastId());
			}
		}

		return new BatchBuilderIterator<>(iterator, 1000);
	}

	public Transaction buildTransactionForBatch(List<Record> batch, MetadataSchemaTypes schemaTypes, RecordProvider recordProvider) {
		SchemaUtils utils = new SchemaUtils();
		Transaction transaction = new Transaction().setSkippingRequiredValuesValidation(true);
		for (Record record : batch) {
			String schemaCode = record.getSchemaCode();

			for (Entry<String, Object> entry : metadataChangedValues.entrySet()) {
				String metadataCode = entry.getKey();
				if (metadataCode.startsWith(utils.getSchemaTypeCode(schemaCode))) {
					if (!metadataCode.startsWith(schemaCode + "_")) {
						metadataCode = schemaCode + "_" + utils.getLocalCodeFromMetadataCode(metadataCode);
					}

					Metadata metadata = schemaTypes.getMetadata(metadataCode);

					record.set(metadata, entry.getValue());
					if (schemaTypes.isRecordTypeMetadata(metadata)) {
						changeSchemaTypeAccordingToTypeLinkedSchema(record, schemaTypes, recordProvider, metadata);
						schemaCode = record.getSchemaCode();
					}
				}
			}
		}
		transaction.addUpdate(batch);
		return transaction;
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
				Record reportRecord = modelLayerFactory.newSearchServices().searchSingleResult(LogicalSearchQueryOperators.from(batchProcessReportSchema)
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

	private void updateBatchProcessReport(BatchProcessReport report, AppLayerFactory appLayerFactory) {
		try {
			Transaction transaction = new Transaction();
			transaction.addUpdate(report.getWrappedRecord());
			transaction.setRecordFlushing(RecordsFlushing.LATER());
			appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
