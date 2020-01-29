package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.api.extensions.RecordFieldFactoryExtension;
import com.constellio.app.entities.batchProcess.ChangeValueOfMetadataBatchAsyncTask;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.extensions.app.BatchProcessingRecordFactoryExtension;
import com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultModel;
import com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultXLSReportWriter;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.CollectionInfoVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessPossibleImpact;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordFieldModification;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordModifications;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRequest;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.params.BatchProcessingSpecialCaseParams;
import com.constellio.model.services.batch.actions.ChangeValueOfMetadataBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.ModificationImpactCalculator;
import com.constellio.model.services.schemas.ModificationImpactCalculatorResponse;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.records.RecordUtils.changeSchemaTypeAccordingToTypeLinkedSchema;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

public class BatchProcessingPresenterService {
	private static final String TMP_BATCH_FILE = "BatchProcessingPresenterService-formatBatchProcessingResults";
	private static final Logger LOGGER = getLogger(BatchProcessingPresenterService.class);
	private final SchemasRecordsServices schemas;
	private final AppLayerFactory appLayerFactory;
	private final ModelLayerFactory modelLayerFactory;
	private final RecordServices recordServices;
	private final SearchServices searchServices;
	private final String collection;
	private final Locale locale;
	private final AppLayerCollectionExtensions extensions;
	private final ModelLayerCollectionExtensions modelLayerExtensions;

	public BatchProcessingPresenterService(String collection, AppLayerFactory appLayerFactory, Locale locale) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.collection = collection;
		this.locale = locale;
		this.schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.extensions = appLayerFactory.getExtensions().forCollection(collection);
		this.modelLayerExtensions = modelLayerFactory.getExtensions().forCollection(collection);
	}

	public String getOriginSchema(String schemaType, String selectedType, LogicalSearchQuery query) {
		long resultsCount = searchServices.getResultsCount(query);
		if (resultsCount == 0) {
			throw new ImpossibleRuntimeException("Batch processing should be done on at least one record");
		}

		if (StringUtils.isNotBlank(selectedType)) {
			Record record = recordServices.getDocumentById(selectedType);
			return schemas.getLinkedSchemaOf(record);
		}

		String schema_s = Schemas.SCHEMA.getDataStoreCode();
		Map<String, List<FacetValue>> schemaFacet = searchServices.query(query.setNumberOfRows(0).addFieldFacet(schema_s))
				.getFieldFacetValues();
		Set<String> schemaList = new HashSet<>();
		for (FacetValue facetValue : schemaFacet.get(schema_s)) {
			if (facetValue.getQuantity() == resultsCount) {
				schemaList.add(facetValue.getValue());
			}
		}

		return schemaList.size() == 1 ? schemaList.iterator().next() : getDefaultSchema(schemaType);
	}

	public String getOriginSchema(String schemaType, String selectedType, List<String> selectedRecordIds) {
		if (selectedRecordIds == null || selectedRecordIds.isEmpty()) {
			throw new ImpossibleRuntimeException("Batch processing should be done on at least one record");
		}

		if (StringUtils.isNotBlank(selectedType)) {
			Record record = recordServices.getDocumentById(selectedType);
			return schemas.getLinkedSchemaOf(record);
		}

		Set<String> schemaList = new HashSet<>();
		for (String recordId : selectedRecordIds) {
			Record record = recordServices.getDocumentById(recordId);
			schemaList.add(record.getSchemaCode());
		}

		return schemaList.size() == 1 ? schemaList.iterator().next() : getDefaultSchema(schemaType);
	}

	private String getDefaultSchema(String schemaType) {
		return schemaType + "_default";
	}

	private String getRecordSchemaCode(RecordServices recordServices, String recordId) {
		return recordServices.getDocumentById(recordId).getSchemaCode();
	}

	private String getSchemataType(Set<String> recordsSchemata) {
		String firstType = getSchemaType(recordsSchemata.iterator().next());
		ensureAllSchemataOfSameType(recordsSchemata, firstType);
		return firstType;
	}

	private String getSchemaType(String schemaCode) {
		return StringUtils.substringBefore(schemaCode, "_");
	}

	private void ensureAllSchemataOfSameType(Set<String> recordsSchemata, String firstType) {
		for (String schemaCode : recordsSchemata) {
			String currentSchemaType = getSchemaType(schemaCode);
			if (!currentSchemaType.equals(firstType)) {
				throw new ImpossibleRuntimeException("Batch processing should be done on the same schema type :" +
													 StringUtils.join(recordsSchemata, ";"));
			}
		}
	}

	public List<String> getDestinationSchemata(String schemaType) {
		List<String> schemataCodes = new ArrayList<>();
		List<MetadataSchema> schemata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(schemaType).getAllSchemas();
		for (MetadataSchema currentSchema : schemata) {
			schemataCodes.add(currentSchema.getCode());
		}
		return schemataCodes;
	}

	public RecordVO newRecordVO(String schemaCode, final SessionContext sessionContext,
								final List<String> selectedRecordIds) {
		final MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(schemaCode);
		Record tmpRecord = modelLayerFactory.newRecordServices().newRecordWithSchema(schema);

		final Map<String, String> customizedLabels = getCustomizedLabels(schemaCode, locale);
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder() {
			@Override
			protected MetadataToVOBuilder newMetadataToVOBuilder() {
				return new MetadataToVOBuilder() {
					@Override
					protected MetadataVO newMetadataVO(short id, String metadataCode, String metadataLocalCode,
													   String datastoreCode,
													   MetadataValueType type, String collection,
													   MetadataSchemaVO schemaVO, boolean required,
													   boolean multivalue, boolean readOnly, boolean unmodifiable,
													   Map<Locale, String> labels, Class<? extends Enum<?>> enumClass,
													   String[] taxonomyCodes,
													   String schemaTypeCode, MetadataInputType metadataInputType,
													   MetadataDisplayType metadataDisplayType,
													   AllowedReferences allowedReferences,
													   boolean enabled, StructureFactory structureFactory,
													   String metadataGroup,
													   Object defaultValue, String inputMask,
													   Set<String> customAttributes, boolean isMultiLingual,
													   Locale locale, Map<String, Object> customParameters,
													   CollectionInfoVO collectionInfoVO, boolean sortable) {
						// Replace labels with customized labels
						String customizedLabel = customizedLabels.get(metadataCode);
						if (customizedLabel != null) {
							for (Locale labelLocale : labels.keySet()) {
								labels.put(labelLocale, customizedLabel);
							}
						}
						// Default value is always null
						required = false;
						defaultValue = null;
						User user = schemas.getUser(sessionContext.getCurrentUser().getId());
						return isMetadataModifiable(metadataCode, user, selectedRecordIds) ?
							   super.newMetadataVO(id, metadataCode, metadataLocalCode, datastoreCode, type, collection, schemaVO, required, multivalue,
									   readOnly,
									   unmodifiable, labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType,
									   metadataDisplayType,
									   allowedReferences,
									   enabled, structureFactory, metadataGroup, defaultValue, inputMask, customAttributes, isMultiLingual, locale, customParameters, collectionInfoVO, sortable) :
							   null;
					}
				};
			}
		};
		MetadataSchemaVO schemaVO = schemaVOBuilder.build(schema, RecordVO.VIEW_MODE.FORM, sessionContext);

		return new RecordToVOBuilder() {
			@Override
			protected Object getValue(Record record, Metadata metadata) {
				return null;
			}
		}.build(tmpRecord, RecordVO.VIEW_MODE.FORM, schemaVO, sessionContext);
	}

	public BatchProcessResults execute(String selectedType, List<String> records, RecordVO viewObject,
									   List<String> metadatasToEmpty, User user)
			throws RecordServicesException {

		BatchProcessAction batchProcessAction = toAction(selectedType, viewObject, metadatasToEmpty);
		BatchProcessRequest request;
		if (records != null && records.size() > 1000) {
			request = toRequest(selectedType, records.subList(0, 1000), viewObject, metadatasToEmpty, user);
		} else {
			request = toRequest(selectedType, records, viewObject, metadatasToEmpty, user);
		}

		return execute(request, batchProcessAction, records, user, "userBatchProcess");
	}

	public BatchProcessResults execute(BatchProcessRequest request, BatchProcessAction action, List<String> records,
									   User user, String title)
			throws RecordServicesException {

		validateUserPermissionForRecordCount(records.size(), user);

		//		System.out.println("**************** EXECUTE ****************");
		//		System.out.println("ACTION : ");
		//		System.out.println(action);
		Transaction transaction = prepareTransactionWithIds(request, true);
		recordServices.validateTransaction(transaction);

		AsyncTask asyncTask = new ChangeValueOfMetadataBatchAsyncTask(request.getModifiedMetadatas(), null, records, Long.valueOf(records.size()));
		AsyncTaskCreationRequest asyncTaskRequest = new AsyncTaskCreationRequest(asyncTask, collection, title);
		asyncTaskRequest.setUsername(user.getUsername());

		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		AsyncTaskBatchProcess batchProcess = batchProcessesManager.addAsyncTask(asyncTaskRequest);
		registerNewBatchProcessEvent(batchProcess, records.size());

		return null;
	}

	private void registerNewBatchProcessEvent(BatchProcess batchProcess, int totalModifiedRecords) {
		LoggingServices loggingServices = modelLayerFactory.newLoggingServices();
		loggingServices.createBatchProcess(batchProcess, totalModifiedRecords);
	}

	public BatchProcessResults simulate(String selectedType, List<String> records, RecordVO viewObject,
										List<String> metadatasToEmpty, User user)
			throws RecordServicesException {
		BatchProcessRequest request;
		if (records != null && records.size() > 100) {
			request = toRequest(selectedType, records.subList(0, 100), viewObject, metadatasToEmpty, user);
		} else {
			request = toRequest(selectedType, records, viewObject, metadatasToEmpty, user);
		}
		return simulateWithIds(request);
	}

	public BatchProcessResults simulateWithIds(BatchProcessRequest request)
			throws RecordServicesException.ValidationException {
		System.out.println("**************** SIMULATE ****************");
		System.out.println("REQUEST : ");
		System.out.println(request);
		Transaction transaction = prepareTransactionWithIds(request, true);
		recordServices.validateTransaction(transaction);
		BatchProcessResults results = toBatchProcessResults(transaction);

		System.out.println("\nRESULTS : ");
		System.out.println(results);
		return results;
	}

	public RecordVO newRecordVO(String schemaCode, final SessionContext sessionContext,
								final LogicalSearchQuery query) {
		final MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(schemaCode);
		Record tmpRecord = modelLayerFactory.newRecordServices().newRecordWithSchema(schema, false);

		final Map<String, String> customizedLabels = getCustomizedLabels(schemaCode, locale);
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder() {
			@Override
			protected MetadataToVOBuilder newMetadataToVOBuilder() {
				return new MetadataToVOBuilder() {
					@Override
					protected MetadataVO newMetadataVO(short id, String metadataCode, String metadataLocalCode,
													   String datastoreCode,
													   MetadataValueType type, String collection,
													   MetadataSchemaVO schemaVO, boolean required,
													   boolean multivalue, boolean readOnly, boolean unmodifiable,
													   Map<Locale, String> labels, Class<? extends Enum<?>> enumClass,
													   String[] taxonomyCodes,
													   String schemaTypeCode, MetadataInputType metadataInputType,
													   MetadataDisplayType metadataDisplayType,
													   AllowedReferences allowedReferences,
													   boolean enabled, StructureFactory structureFactory,
													   String metadataGroup,
													   Object defaultValue, String inputMask,
													   Set<String> customAttributes, boolean isMultiLingual,
													   Locale locale, Map<String, Object> customParameters,
													   CollectionInfoVO collectionInfoVO, boolean sortable) {
						// Replace labels with customized labels
						String customizedLabel = customizedLabels.get(metadataCode);
						if (customizedLabel != null) {
							for (Locale labelLocale : labels.keySet()) {
								labels.put(labelLocale, customizedLabel);
							}
						}
						// Default value is always null
						required = false;
						defaultValue = null;
						//						User user = schemas.getUser(sessionContext.getCurrentUser().getId());
						Map<String, List<FacetValue>> fieldFacetValues = searchServices
								.query(query.addFieldFacet("schema_s").setNumberOfRows(0)).getFieldFacetValues();
						for (FacetValue facetValue : fieldFacetValues.get("schema_s")) {
							long resultCountForFacetValue = facetValue.getQuantity();
							String schemaCode = facetValue.getValue();
							MetadataSchema schema = schemas.schema(schemaCode);
							try {
								Metadata metadata = schema.get(metadataCode);
								if (resultCountForFacetValue > 0 && metadata.isUnmodifiable()) {
									return null;
								}
							} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
								continue;
							}
						}
						return super.newMetadataVO(id, metadataCode, metadataLocalCode, datastoreCode, type, collection, schemaVO, required, multivalue,
								readOnly,
								unmodifiable, labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType,
								metadataDisplayType,
								allowedReferences,
								enabled, structureFactory, metadataGroup, defaultValue, inputMask, customAttributes, isMultiLingual, locale, customParameters, collectionInfoVO, sortable);
					}
				};
			}
		};
		MetadataSchemaVO schemaVO = schemaVOBuilder.build(schema, RecordVO.VIEW_MODE.FORM, sessionContext);

		return new RecordToVOBuilder() {
			@Override
			protected Object getValue(Record record, Metadata metadata) {
				return null;
			}
		}.build(tmpRecord, RecordVO.VIEW_MODE.FORM, schemaVO, sessionContext);
	}

	public BatchProcessResults execute(String selectedType, LogicalSearchQuery query, RecordVO viewObject,
									   List<String> metadatasToEmpty, User user)
			throws RecordServicesException {
		LogicalSearchQuery validationQuery = query;
		validationQuery = validationQuery.setNumberOfRows(1000);
		BatchProcessAction batchProcessAction = toAction(selectedType, viewObject, metadatasToEmpty);
		BatchProcessRequest request = toRequest(selectedType, validationQuery, viewObject, metadatasToEmpty, user);
		return execute(request, batchProcessAction, query, user, "userBatchProcess");

	}

	public BatchProcessResults execute(BatchProcessRequest request, BatchProcessAction action, LogicalSearchQuery query,
									   User user, String title)
			throws RecordServicesException {

		validateUserPermissionForRecordCount(query, user);

		//		System.out.println("**************** EXECUTE ****************");
		//		System.out.println("ACTION : ");
		//		System.out.println(action);
		List<Transaction> transactionList = prepareTransactionWithQuery(request, true);
		RMConfigs rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		for (Transaction transaction : transactionList) {
			if (rmConfigs.isIgnoreValidationsInBatchProcessing()) {
				transaction.setOptions(RecordUpdateOptions.userModificationsSafeOptions());
			}
			recordServices.validateTransaction(transaction);
		}

		long totalModifiedRecords = searchServices.getResultsCount(query);
		AsyncTask asyncTask = new ChangeValueOfMetadataBatchAsyncTask(request.getModifiedMetadatas(), toQueryString(query), null, totalModifiedRecords);
		AsyncTaskCreationRequest asyncTaskRequest = new AsyncTaskCreationRequest(asyncTask, collection, title);
		asyncTaskRequest.setUsername(user.getUsername());

		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		AsyncTaskBatchProcess batchProcess = batchProcessesManager.addAsyncTask(asyncTaskRequest);
		registerNewBatchProcessEvent(batchProcess, (int) totalModifiedRecords);

		return null;
	}

	private String toQueryString(LogicalSearchQuery query) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		ModifiableSolrParams params = searchServices.addSolrModifiableParams(query);
		String solrQuery = SolrUtils.toSingleQueryString(params);
		return solrQuery;
	}

	public BatchProcessResults simulate(String selectedType, LogicalSearchQuery query, RecordVO viewObject,
										List<String> metadatasToEmpty, User user)
			throws RecordServicesException {
		BatchProcessRequest request = toRequest(selectedType, query, viewObject, metadatasToEmpty, user);
		return simulateWithQuery(request);
	}

	public BatchProcessResults simulateWithQuery(BatchProcessRequest request)
			throws RecordServicesException.ValidationException {
		System.out.println("**************** SIMULATE ****************");
		System.out.println("REQUEST : ");
		System.out.println(request);
		List<Transaction> transactionList = prepareTransactionWithQuery(request, true);

		for (Transaction transaction : transactionList) {
			recordServices.validateTransaction(transaction);
		}

		BatchProcessResults results = null;
		if (transactionList.size() == 1) {
			results = toBatchProcessResults(transactionList.get(0));
		} else {
			results = toBatchProcessResults(transactionList);
		}

		System.out.println("\nRESULTS : ");
		System.out.println(results);
		return results;
	}

	private BatchProcessResults toBatchProcessResults(List<Transaction> transactionList) {

		List<BatchProcessRecordModifications> recordModificationses = new ArrayList<>();
		for (Transaction transaction : transactionList) {
			for (Record record : transaction.getModifiedRecords()) {

				List<BatchProcessRecordFieldModification> recordFieldModifications = new ArrayList<>();
				List<BatchProcessPossibleImpact> impacts = new ArrayList<>();
				Record originalRecord = record.getCopyOfOriginalRecord();
				for (Metadata metadata : record.getModifiedMetadatas(schemas.getTypes())) {
					if (!Schemas.isGlobalMetadataExceptTitle(metadata.getLocalCode()) && extensions
							.isMetadataDisplayedWhenModifiedInBatchProcessing(metadata)) {
						String valueBefore = convertToString(metadata, originalRecord.get(metadata));
						String valueAfter = convertToString(metadata, record.get(metadata));
						recordFieldModifications.add(new BatchProcessRecordFieldModification(valueBefore, valueAfter, metadata));
					}
				}

				List<Taxonomy> taxonomies = modelLayerFactory.getTaxonomiesManager().getEnabledTaxonomies(collection);
				ModificationImpactCalculatorResponse response = new ModificationImpactCalculator(
						schemas.getTypes(), taxonomies, searchServices, recordServices).findTransactionImpact(transaction, true);
				transaction.addAllRecordsToReindex(response.getRecordsToReindexLater());
				for (ModificationImpact impact : response.getImpacts()) {
					impacts.add(
							new BatchProcessPossibleImpact(impact.getPotentialImpactsCount(), impact.getImpactedSchemaType()));
				}

				recordModificationses.add(new BatchProcessRecordModifications(originalRecord.getId(), originalRecord.getTitle(),
						impacts, recordFieldModifications));
			}
		}

		return new BatchProcessResults(recordModificationses);
	}

	private BatchProcessResults toBatchProcessResults(Transaction transaction) {

		MetadataSchemaTypes types = schemas.getTypes();
		List<BatchProcessRecordModifications> recordModificationses = new ArrayList<>();
		for (Record record : transaction.getModifiedRecords()) {

			Record originalRecord = record.getCopyOfOriginalRecord();
			List<Metadata> modifiedMetadatas = new ArrayList<>();
			modifiedMetadatas.addAll(record.getModifiedMetadatas(types));
			recordServices.recalculate(originalRecord);
			recordServices.recalculate(record);
			//			recordServices.loadLazyTransientMetadatas(originalRecord);
			//			recordServices.reloadEagerTransientMetadatas(originalRecord);
			//			recordServices.loadLazyTransientMetadatas(record);
			//			recordServices.reloadEagerTransientMetadatas(record);

			for (Metadata metadata : types.getSchemaOf(record).getLazyTransientMetadatas()) {
				if (!LangUtils.isEqual(record.get(metadata), originalRecord.get(metadata))) {
					if (!Schemas.isGlobalMetadataExceptTitle(metadata.getLocalCode()) && extensions
							.isMetadataDisplayedWhenModifiedInBatchProcessing(metadata)) {
						modifiedMetadatas.add(metadata);
					}
				}
			}

			for (Metadata metadata : types.getSchemaOf(record).getEagerTransientMetadatas()) {
				if (!LangUtils.isEqual(record.get(metadata), originalRecord.get(metadata))) {
					if (!Schemas.isGlobalMetadataExceptTitle(metadata.getLocalCode()) && extensions
							.isMetadataDisplayedWhenModifiedInBatchProcessing(metadata)) {
						modifiedMetadatas.add(metadata);
					}
				}
			}

			List<BatchProcessRecordFieldModification> recordFieldModifications = new ArrayList<>();
			List<BatchProcessPossibleImpact> impacts = new ArrayList<>();
			for (Metadata metadata : modifiedMetadatas) {
				if (!Schemas.isGlobalMetadataExceptTitle(metadata.getLocalCode()) && extensions
						.isMetadataDisplayedWhenModifiedInBatchProcessing(metadata)) {
					String valueBefore = convertToString(metadata, originalRecord.get(metadata));
					String valueAfter = convertToString(metadata, record.get(metadata));
					recordFieldModifications.add(new BatchProcessRecordFieldModification(valueBefore, valueAfter, metadata));
				}
			}

			List<Taxonomy> taxonomies = modelLayerFactory.getTaxonomiesManager().getEnabledTaxonomies(collection);
			ModificationImpactCalculatorResponse modificationImpactCalculatorResponse =
					new ModificationImpactCalculator(schemas.getTypes(), taxonomies, searchServices,
							recordServices).findTransactionImpact(transaction, true);
			transaction.addAllRecordsToReindex(modificationImpactCalculatorResponse.getRecordsToReindexLater());
			for (ModificationImpact impact : modificationImpactCalculatorResponse.getImpacts()) {
				impacts.add(new BatchProcessPossibleImpact(impact.getPotentialImpactsCount(), impact.getImpactedSchemaType()));
			}

			recordModificationses.add(new BatchProcessRecordModifications(originalRecord.getId(), originalRecord.getTitle(),
					impacts, recordFieldModifications));
		}

		return new BatchProcessResults(recordModificationses);
	}

	private String convertToString(Metadata metadata, Object value) {
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
					stringBuilder.append(convertScalarToString(metadata, item));
				}

				stringBuilder.append("]");

				return stringBuilder.toString();
			} else {
				return convertScalarToString(metadata, value);
			}
		} catch (Exception e) {
			LOGGER.warn("Cannot format unsupported value '" + value + "'", e);
			return "?";
		}
	}

	private String convertScalarToString(Metadata metadata, Object value) {
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
				Record record = recordServices.getDocumentById(value.toString());
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

	public List<Transaction> prepareTransactionWithQuery(final BatchProcessRequest request, boolean recalculate) {
		final MetadataSchemaTypes types = schemas.getTypes();
		final List<Transaction> transactionList = new ArrayList<>();
		Transaction transaction = new Transaction();
		int counter = 0;
		Map<String, Map<String, Object>> specialCaseModificationByRecordId = new HashMap<>();

		List<Record> recordList = searchServices.search(request.getQuery());
		for (Record record : recordList) {
			transaction.add(record);
			if (++counter == 1000) {
				counter = 0;
				transactionList.add(transaction);
				transaction = new Transaction();
			}
			MetadataSchema currentRecordSchema = types.getSchemaOf(record);

			for (Map.Entry<String, Object> entry : request.getModifiedMetadatas().entrySet()) {
				String localMetadataCode = new SchemaUtils().getLocalCodeFromMetadataCode(entry.getKey());
				String metadataCode = currentRecordSchema.getCode() + "_" + localMetadataCode;
				if (currentRecordSchema.hasMetadataWithCode(metadataCode)) {
					Metadata metadata = currentRecordSchema.get(metadataCode);

					if (types.isRecordTypeMetadata(metadata)) {
						record.set(metadata, entry.getValue());
						changeSchemaTypeAccordingToTypeLinkedSchema(record, types, new RecordProvider(recordServices), metadata);
					}
				}
			}

			currentRecordSchema = types.getSchemaOf(record);
			for (Map.Entry<String, Object> entry : request.getModifiedMetadatas().entrySet()) {
				String localMetadataCode = new SchemaUtils().getLocalCodeFromMetadataCode(entry.getKey());

				String metadataCode = currentRecordSchema.getCode() + "_" + localMetadataCode;
				if (currentRecordSchema.hasMetadataWithCode(metadataCode)) {
					Metadata metadata = currentRecordSchema.get(currentRecordSchema.getCode() + "_" + localMetadataCode);
					record.set(metadata, entry.getValue());
				}
			}


			Map<String, Object> temporaryMetadataChangeHash = modelLayerFactory.getExtensions()
					.forCollection(collection)
					.batchProcessingSpecialCaseExtensions(new BatchProcessingSpecialCaseParams(record, request.getUser()));
			if (temporaryMetadataChangeHash.size() > 0) {
				specialCaseModificationByRecordId.put(record.getId(), temporaryMetadataChangeHash);
			}
		}

		request.setSpecialCaseModifiedMetadatas(specialCaseModificationByRecordId);
		if (counter < 1000) {
			transactionList.add(transaction);
		}

		return transactionList;
	}

	public Transaction prepareTransactionWithIds(BatchProcessRequest request, boolean recalculate) {
		Transaction transaction = new Transaction();
		Map<String, Map<String, Object>> specialCaseModificationByRecordId = new HashMap<>();

		MetadataSchemaTypes types = schemas.getTypes();
		for (String id : request.getIds()) {
			Record record = recordServices.getDocumentById(id);
			transaction.add(record);
			MetadataSchema currentRecordSchema = types.getSchemaOf(record);

			for (Map.Entry<String, Object> entry : request.getModifiedMetadatas().entrySet()) {
				String localMetadataCode = new SchemaUtils().getLocalCodeFromMetadataCode(entry.getKey());
				String metadataCode = currentRecordSchema.getCode() + "_" + localMetadataCode;
				if (currentRecordSchema.hasMetadataWithCode(metadataCode)) {
					Metadata metadata = currentRecordSchema.get(metadataCode);

					if (types.isRecordTypeMetadata(metadata)) {
						record.set(metadata, entry.getValue());
						changeSchemaTypeAccordingToTypeLinkedSchema(record, types, new RecordProvider(recordServices), metadata);
					}
				}
			}

			currentRecordSchema = types.getSchemaOf(record);
			for (Map.Entry<String, Object> entry : request.getModifiedMetadatas().entrySet()) {
				String localMetadataCode = new SchemaUtils().getLocalCodeFromMetadataCode(entry.getKey());

				String metadataCode = currentRecordSchema.getCode() + "_" + localMetadataCode;
				if (currentRecordSchema.hasMetadataWithCode(metadataCode)) {
					Metadata metadata = currentRecordSchema.get(currentRecordSchema.getCode() + "_" + localMetadataCode);
					record.set(metadata, entry.getValue());
				}
			}

			Map<String, Object> temporaryMetadataChangeHash = modelLayerFactory.getExtensions()
					.forCollection(collection)
					.batchProcessingSpecialCaseExtensions(new BatchProcessingSpecialCaseParams(record, request.getUser()));
			if (temporaryMetadataChangeHash.size() > 0) {
				specialCaseModificationByRecordId.put(record.getId(), temporaryMetadataChangeHash);
			}
		}

		request.setSpecialCaseModifiedMetadatas(specialCaseModificationByRecordId);

		if (recalculate) {
			for (Record record : transaction.getModifiedRecords()) {
				recordServices.recalculate(record);
			}
		}

		return transaction;
	}

	private boolean isNonEmptyValue(Metadata metadata, Object o) {
		if (metadata.isMultivalue()) {
			return o != null && o instanceof List && !((List) o).isEmpty();
		} else {
			return o != null && !"".equals(o);
		}
	}

	public boolean isMetadataModifiable(String metadataCode, User user, List<String> selectedRecordIds) {

		boolean metadataModifiable = true;
		for (String selectedRecordId : selectedRecordIds) {
			Metadata metadata = schemas.getTypes().getMetadata(metadataCode);
			metadataModifiable &= extensions.isMetadataModifiableInBatchProcessing(metadata, user, selectedRecordId);
		}
		return metadataModifiable;
	}

	public String getTypeSchemaType(String schemaType) {
		return schemas.getRecordTypeMetadataOf(schemas.getTypes().getSchemaType(schemaType)).getReferencedSchemaTypeCode();
	}

	public Map<String, String> getCustomizedLabels(String schemaCode, Locale locale) {
		Provider<String, String> provider = new Provider<String, String>() {
			@Override
			public String get(String key) {
				return $(key);
			}
		};
		MetadataSchema schema = schemas.getTypes().getSchema(schemaCode);
		return extensions.getCustomLabels(schema, locale, provider);
	}

	public RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType, LogicalSearchQuery query) {
		BatchProcessingRecordFactoryExtension.BatchProcessingFieldFactoryExtensionParams params =
				new BatchProcessingRecordFactoryExtension.BatchProcessingFieldFactoryExtensionParams(
						BatchProcessingRecordFactoryExtension.BATCH_PROCESSING_FIELD_FACTORY_KEY, null, schemaType, query);
		//params.setSelectedTypeId(selectedType);

		RecordFieldFactory recordFieldFactory = null;
		VaultBehaviorsList<RecordFieldFactoryExtension> recordFieldFactoryExtensions = appLayerFactory.getExtensions()
				.forCollection(collection).recordFieldFactoryExtensions;
		for (RecordFieldFactoryExtension extension : recordFieldFactoryExtensions) {
			recordFieldFactory = extension.newRecordFieldFactory(params);
			if (recordFieldFactory != null) {
				break;
			}
		}
		return recordFieldFactory;
	}

	public RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType,
													List<String> selectedRecordIds) {
		BatchProcessingRecordFactoryExtension.BatchProcessingFieldFactoryExtensionParams params =
				new BatchProcessingRecordFactoryExtension.BatchProcessingFieldFactoryExtensionParams(
						BatchProcessingRecordFactoryExtension.BATCH_PROCESSING_FIELD_FACTORY_KEY, null, schemaType,
						selectedRecordIds);
		//params.setSelectedTypeId(selectedType).setSelectedRecords(selectedRecordIds);

		RecordFieldFactory recordFieldFactory = null;
		VaultBehaviorsList<RecordFieldFactoryExtension> recordFieldFactoryExtensions = appLayerFactory.getExtensions()
				.forCollection(collection).recordFieldFactoryExtensions;
		for (RecordFieldFactoryExtension extension : recordFieldFactoryExtensions) {
			recordFieldFactory = extension.newRecordFieldFactory(params);
			if (recordFieldFactory != null) {
				break;
			}
		}
		return recordFieldFactory;
	}

	private void validateUserPermissionForRecordCount(LogicalSearchQuery query, User user)
			throws RecordServicesException {
		int recordCount = (int) searchServices.getResultsCount(query);
		validateUserPermissionForRecordCount(recordCount, user);
	}

	private void validateUserPermissionForRecordCount(int recordCount, User user) throws RecordServicesException {
		if (!user.has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).globally()) {
			throw new RecordServicesException($("BatchProcess.batchProcessPermissionMissing"));
		}

		if (!user.has(CorePermissions.MODIFY_UNLIMITED_RECORDS_USING_BATCH_PROCESS).globally()) {
			ConstellioEIMConfigs systemConfigs = modelLayerFactory.getSystemConfigs();
			int batchProcessingLimit = systemConfigs.getBatchProcessingLimit();
			if (batchProcessingLimit != 0 && recordCount > batchProcessingLimit) {
				throw new RecordServicesException($("BatchProcess.batchProcessUnlimitedPermissionMissing", batchProcessingLimit));
			}
		}
	}

	public boolean hasWriteAccessOnAllRecords(User user, List<String> selectedRecordIds) {

		boolean writeAccess = true;
		for (String selectedRecordId : selectedRecordIds) {
			Record record = recordServices.getDocumentById(selectedRecordId);
			writeAccess &= user.hasWriteAccess().on(record) && modelLayerExtensions.isRecordModifiableBy(record, user);
		}
		return writeAccess;
	}

	public AppLayerCollectionExtensions getBatchProcessingExtension() {
		return appLayerFactory.getExtensions().forCollection(collection);
	}

	private static List<String> excludedMetadatas = asList(Schemas.IDENTIFIER.getLocalCode(), Schemas.CREATED_ON.getLocalCode(),
			Schemas.MODIFIED_ON.getLocalCode(), RMObject.FORM_CREATED_ON, RMObject.FORM_MODIFIED_ON);

	public BatchProcessRequest toRequest(String selectedType, LogicalSearchQuery query, RecordVO formVO,
										 List<String> metadatasToEmpty, User user) throws RecordServicesException {
		String typeCode = new SchemaUtils().getSchemaTypeCode(formVO.getSchema().getCode());
		MetadataSchemaType type = schemas.getTypes().getSchemaType(typeCode);
		Map<String, Object> fieldsModifications = getFieldsModifications(selectedType, formVO, metadatasToEmpty);

		query.setPreferAnalyzedFields(true);
		return new BatchProcessRequest(null, query, user, type, fieldsModifications);
	}

	public BatchProcessRequest toRequest(String selectedType, List<String> selectedRecord, RecordVO formVO,
										 List<String> metadatasToEmpty, User user) throws RecordServicesException {
		String typeCode = new SchemaUtils().getSchemaTypeCode(formVO.getSchema().getCode());
		MetadataSchemaType type = schemas.getTypes().getSchemaType(typeCode);
		Map<String, Object> fieldsModifications = getFieldsModifications(selectedType, formVO, metadatasToEmpty);

		return new BatchProcessRequest(selectedRecord, null, user, type, fieldsModifications);
	}

	private Map<String, Object> getFieldsModifications(String selectedType, RecordVO formVO,
													   List<String> metadatasToEmpty) throws RecordServicesException {
		String typeCode = new SchemaUtils().getSchemaTypeCode(formVO.getSchema().getCode());
		MetadataSchemaType type = schemas.getTypes().getSchemaType(typeCode);
		MetadataSchema schema = schemas.getTypes().getSchema(formVO.getSchema().getCode());
		Map<String, Object> fieldsModifications = new HashMap<>();
		for (MetadataVO metadataVO : formVO.getMetadatas()) {
			Metadata metadata = schema.get(metadataVO.getLocalCode());
			Object value = formVO.get(metadataVO);

			LOGGER.info(metadata.getCode() + ":" + value);
			if ((metadata.getDataEntry().getType() == DataEntryType.MANUAL
				 || isCalculatedWithEvaluator(metadata))
				&& (!metadata.isSystemReserved() || Schemas.TITLE_CODE.equals(metadata.getLocalCode()))
				&& !excludedMetadatas.contains(metadata.getLocalCode())) {

				boolean isModified = isNonEmptyValue(metadata, value);
				boolean isEmptied = metadatasToEmpty.contains(metadataVO.getLocalCode());
				if (isModified && isEmptied) {
					throw new RecordServicesException(
							$("BatchProcess.batchProcessCannotEmptyAndModify", metadata.getLocalCode()));
				} else if (isModified || isEmptied) {
					LOGGER.info("");
					fieldsModifications.put(metadataVO.getCode(), value);
				}
			}
		}
		if (org.apache.commons.lang3.StringUtils.isNotBlank(selectedType)) {
			Metadata typeMetadata = schemas.getRecordTypeMetadataOf(type);
			LOGGER.info(typeMetadata.getCode() + ":" + selectedType);
			fieldsModifications.put(typeMetadata.getCode(), selectedType);
		}

		return fieldsModifications;
	}

	private boolean isCalculatedWithEvaluator(Metadata metadata) {
		return metadata.getDataEntry().getType() == DataEntryType.CALCULATED
			   && ((CalculatedDataEntry) metadata.getDataEntry()).getCalculator().hasEvaluator();
	}

	public BatchProcessAction toAction(String selectedType, RecordVO formVO, List<String> metadatasToEmpty) {

		String typeCode = new SchemaUtils().getSchemaTypeCode(formVO.getSchema().getCode());
		MetadataSchemaType type = schemas.getTypes().getSchemaType(typeCode);
		MetadataSchema schema = schemas.getTypes().getSchema(formVO.getSchema().getCode());
		Map<String, Object> fieldsModifications = new HashMap<>();
		for (MetadataVO metadataVO : formVO.getMetadatas()) {
			Metadata metadata = schema.get(metadataVO.getLocalCode());
			Object value = formVO.get(metadataVO);

			LOGGER.info(metadata.getCode() + ":" + value);
			if ((metadata.getDataEntry().getType() == DataEntryType.MANUAL
				 || isCalculatedWithEvaluator(metadata))
				&& (isNonEmptyValue(metadata, value) || metadatasToEmpty.contains(metadataVO.getLocalCode()))
				&& (!metadata.isSystemReserved() || Schemas.TITLE_CODE.equals(metadata.getLocalCode()))
				&& !excludedMetadatas.contains(metadata.getLocalCode())) {

				LOGGER.info("");
				fieldsModifications.put(metadataVO.getCode(), value);
			}
		}
		if (org.apache.commons.lang3.StringUtils.isNotBlank(selectedType)) {
			Metadata typeMetadata = schemas.getRecordTypeMetadataOf(type);
			LOGGER.info(typeMetadata.getCode() + ":" + selectedType);
			fieldsModifications.put(typeMetadata.getCode(), selectedType);
		}

		return new ChangeValueOfMetadataBatchProcessAction(fieldsModifications);
	}

	public InputStream formatBatchProcessingResults(BatchProcessResults results) {
		Language locale = i18n.getLanguage();
		File resultsFile = null;
		Closeable outputStream = null;
		IOServices ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		try {
			resultsFile = ioServices.newTemporaryFile(TMP_BATCH_FILE);
			outputStream = new FileOutputStream(resultsFile);
			new BatchProcessingResultXLSReportWriter(new BatchProcessingResultModel(results, locale), i18n.getLocale())
					.write((OutputStream) outputStream);
			IOUtils.closeQuietly(outputStream);
			return new FileInputStream(resultsFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ioServices.deleteQuietly(resultsFile);
			IOUtils.closeQuietly(outputStream);
		}
	}
}