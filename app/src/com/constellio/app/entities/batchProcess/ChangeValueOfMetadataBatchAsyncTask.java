package com.constellio.app.entities.batchProcess;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultCSVReportWriter;
import com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultModel;
import com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultXLSReportWriter;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessPossibleImpact;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordFieldModification;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordModifications;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.*;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.batch.controller.RecordFromIdListIterator;
import com.constellio.model.services.batch.state.BatchProcessProgressionServices;
import com.constellio.model.services.batch.state.InMemoryBatchProcessProgressionServices;
import com.constellio.model.services.batch.state.StoredBatchProcessPart;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.iterators.RecordSearchResponseIterator;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.records.RecordUtils.changeSchemaTypeAccordingToTypeLinkedSchema;
import static java.util.Arrays.asList;

public class ChangeValueOfMetadataBatchAsyncTask implements AsyncTask {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChangeValueOfMetadataBatchAsyncTask.class);
	private File csvReport = null;
	private OutputStream csvOuputStream = null;

	final Map<String, Object> metadataChangedValues;
	Long totalNumberOfRecords;
	final String query;
	List<String> recordIds;

	public ChangeValueOfMetadataBatchAsyncTask(Map<String, Object> metadataChangedValues, String query, List<String> recordIds, Long totalNumberOfRecords) {
		this.metadataChangedValues = new HashMap<>(metadataChangedValues);
		this.query = query;
		this.recordIds = recordIds;
		this.totalNumberOfRecords = totalNumberOfRecords;
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[] { metadataChangedValues, query, recordIds, totalNumberOfRecords };
	}

	@Override
	public void execute(AsyncTaskExecutionParams params) throws ValidationException {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		ContentManager contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		AsyncTaskBatchProcess batchProcess = params.getBatchProcess();
		BatchProcessReport report = getLinkedBatchProcessReport(batchProcess, appLayerFactory);
		int numberOfRecordsPerTask = 100;
		InputStream csvInputStream = null;

		BatchProcessProgressionServices batchProcessProgressionServices = new InMemoryBatchProcessProgressionServices();
		StoredBatchProcessPart previousPart = batchProcessProgressionServices.getLastBatchProcessPart(batchProcess);
		BatchBuilderIterator<Record> batchIterator = getBatchIterator(appLayerFactory, previousPart);

		MetadataSchemaTypes schemaTypes = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(params.getCollection());
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		RecordProvider recordProvider = new RecordProvider(recordServices);

		params.setProgressionUpperLimit(totalNumberOfRecords);

		try {
			while (batchIterator.hasNext()) {
				List<Record> records = batchIterator.next();
				for (int i = 0; i < records.size(); i += numberOfRecordsPerTask) {
					List<Record> recordsForTransaction = records.subList(i, Math.min(records.size(), i + numberOfRecordsPerTask));
					Transaction transaction = buildTransactionForBatch(recordsForTransaction, schemaTypes, recordProvider);
					try {
						recordServices.prepareRecords(transaction);
						appendCsvReport(transaction, appLayerFactory, params);
						recordServices.execute(transaction);
						transaction.getModifiedRecords();
					} catch (Throwable t) {
						if (report != null) {
							report.appendErrors(asList(t.getMessage()));
						}
						t.printStackTrace();
						LOGGER.error("Error while executing batch process action", t);
						report.addSkippedRecords(transaction.getRecordIds());
					}
					params.incrementProgression(recordsForTransaction.size());
					updateBatchProcessReport(report, appLayerFactory, RecordsFlushing.LATER());
					FileUtils.deleteQuietly(csvReport);
				}
			}

			csvInputStream = new FileInputStream(csvReport);
			ContentVersionDataSummary contentVersion = contentManager.upload(csvInputStream, csvReport.getName()).getContentVersionDataSummary();
			Content content = contentManager.createMajor(userServices.getUserInCollection(batchProcess.getUsername(), batchProcess.getCollection()),
					csvReport.getName(), contentVersion);
			report.setContent(content);
			updateBatchProcessReport(report, appLayerFactory, RecordsFlushing.NOW());
		} catch (IOException e) {
			LOGGER.error("Error occured when creating csv report for batchProcess " + batchProcess.getId());
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(csvInputStream);
			IOUtils.closeQuietly(csvOuputStream);
		}
	}

	private void appendCsvReport(Transaction transaction, AppLayerFactory appLayerFactory, AsyncTaskExecutionParams params)
			throws IOException {
		BatchProcessResults batchProcessResults = toBatchProcessResults(transaction, appLayerFactory, params.getCollection());
		writeBatchProcessingResultsToCsvReport(batchProcessResults, appLayerFactory, params);
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

	private Transaction buildTransactionForBatch(List<Record> batch, MetadataSchemaTypes schemaTypes, RecordProvider recordProvider) {
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

	private void updateBatchProcessReport(BatchProcessReport report, AppLayerFactory appLayerFactory, RecordsFlushing recordsFlushing) {
		try {
			Transaction transaction = new Transaction();
			transaction.addUpdate(report.getWrappedRecord());
			transaction.setRecordFlushing(recordsFlushing);
			appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addRecordsIdsToErrorList(List<Record> batch, List<String> errors) {
		for (Record record : batch) {
			errors.add(record.getId());
		}
	}

	private BatchProcessResults toBatchProcessResults(Transaction transaction, AppLayerFactory appLayerFactory, String collection) {

		List<BatchProcessRecordModifications> recordModificationses = new ArrayList<>();
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		MetadataSchemasManager schemas = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		List<String> collectionLanguages = appLayerFactory.getCollectionsManager().getCollectionLanguages(collection);
		Locale locale = Language.withCode(collectionLanguages.get(0)).getLocale();
		for (Record record : transaction.getModifiedRecords()) {
			Record originalRecord = record.getCopyOfOriginalRecord();

			List<BatchProcessRecordFieldModification> recordFieldModifications = new ArrayList<>();
			for (Metadata metadata : record.getModifiedMetadatas(schemas.getSchemaTypes(collection))) {
				if (!Schemas.isGlobalMetadataExceptTitle(metadata.getLocalCode()) && extensions
						.isMetadataDisplayedWhenModifiedInBatchProcessing(metadata)) {

					String valueBefore = convertToString(metadata, originalRecord.get(metadata), locale, appLayerFactory);
					String valueAfter = convertToString(metadata, record.get(metadata), locale, appLayerFactory);
					recordFieldModifications.add(new BatchProcessRecordFieldModification(valueBefore, valueAfter, metadata));
				}
			}

			recordModificationses.add(new BatchProcessRecordModifications(originalRecord.getId(), originalRecord.getTitle(),
					new ArrayList<BatchProcessPossibleImpact>(), recordFieldModifications));
		}

		return new BatchProcessResults(recordModificationses);
	}

	private String convertToString(Metadata metadata, Object value, Locale locale, AppLayerFactory appLayerFactory) {
		try {
			if (value == null) {
				return null;

			} else if (metadata.isMultivalue()) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("[");
				List<Object> list = (List<Object>) value;

				for (Object item : list) {
					if (stringBuilder.length() > 1) {
						stringBuilder.append(", ");
					}
					stringBuilder.append(convertScalarToString(metadata, item, locale, appLayerFactory));
				}

				stringBuilder.append("]");

				return stringBuilder.toString();
			} else {
				return convertScalarToString(metadata, value, locale, appLayerFactory);
			}
		} catch (Exception e) {
			LOGGER.warn("Cannot format unsupported value '" + value + "'", e);
			return "?";
		}
	}

	private String convertScalarToString(Metadata metadata, Object value, Locale locale, AppLayerFactory appLayerFactory) {
		if (value == null) {
			return null;
		}
		switch (metadata.getType()) {

			case DATE:
				return DateFormatUtils.format((LocalDate) value);

			case DATE_TIME:
				return DateFormatUtils.format((LocalDateTime) value);

			case STRING:
			case TEXT:
				return value.toString();

			case INTEGER:
			case NUMBER:
				return value.toString();

			case BOOLEAN:
				return $(value.toString(), locale);

			case REFERENCE:
				Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(value.toString());
				String code = record.get(Schemas.CODE);
				if (code == null) {
					return record.getId() + " (" + record.getTitle() + ")";
				} else {
					return code + " (" + record.getTitle() + ")";
				}

			case CONTENT:
				return ((Content) value).getCurrentVersion().getFilename();

			case STRUCTURE:
				return value.toString();

			case ENUM:
				return $(metadata.getEnumClass().getSimpleName() + "." + ((EnumWithSmallCode) value).getCode(), locale);
		}

		throw new ImpossibleRuntimeException("Unsupported type : " + metadata.getType());
	}

	private void writeBatchProcessingResultsToCsvReport(BatchProcessResults results, AppLayerFactory appLayerFactory, AsyncTaskExecutionParams params)
			throws IOException {
		List<String> collectionLanguages = appLayerFactory.getCollectionsManager().getCollectionLanguages(params.getCollection());
		Language language = Language.withCode(collectionLanguages.get(0));
		Locale locale = language.getLocale();
		ensureCsvOutputStreamIsInitialized(params);
		new BatchProcessingResultCSVReportWriter(new BatchProcessingResultModel(results, language), locale)
				.write(csvOuputStream);
	}

	private OutputStream ensureCsvOutputStreamIsInitialized(AsyncTaskExecutionParams params)
			throws IOException {
		return csvOuputStream = FileUtils.openOutputStream(createOrGetTempFile(params), true);
	}

	private File createOrGetTempFile(AsyncTaskExecutionParams params) {
		if(csvReport == null) {
			return csvReport = new File(new FoldersLocator().getWorkFolder(), params.getBatchProcess().getId() + File.separator + "batchProcessReport.csv");
		} else {
			return csvReport;
		}
	}
}