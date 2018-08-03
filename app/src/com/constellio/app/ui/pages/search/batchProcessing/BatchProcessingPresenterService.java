package com.constellio.app.ui.pages.search.batchProcessing;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.records.RecordUtils.changeSchemaTypeAccordingToTypeLinkedSchema;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

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

import com.constellio.app.ui.framework.buttons.SIPButton.ChangeValueOfMetadataBatchAsyncTask;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.frameworks.validation.ValidationException;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import com.constellio.app.api.extensions.RecordFieldFactoryExtension;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.extensions.app.BatchProcessingRecordFactoryExtension;
import com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultModel;
import com.constellio.app.modules.rm.reports.builders.BatchProssessing.BatchProcessingResultReportWriter;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.services.factories.AppLayerFactory;
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
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
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
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.batch.actions.ChangeValueOfMetadataBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
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

	public String getOriginType(LogicalSearchQuery query) {
		long resultsCount = searchServices.getResultsCount(query);
		if (resultsCount == 0) {
			throw new ImpossibleRuntimeException("Batch processing should be done on at least one record");
		}
		Map<String, List<FacetValue>> typeId_s = searchServices.query(query.setNumberOfRows(0).addFieldFacet("typeId_s"))
				.getFieldFacetValues();
		Set<String> types = new HashSet<>();
		for (FacetValue facetValue : typeId_s.get("typeId_s")) {
			if (facetValue.getQuantity() == resultsCount) {
				types.add(facetValue.getValue());
			}
		}
		return types.size() == 1 ? types.iterator().next() : null;
	}

	public String getOriginType(List<String> selectedRecordIds) {
		if (selectedRecordIds == null || selectedRecordIds.isEmpty()) {
			throw new ImpossibleRuntimeException("Batch processing should be done on at least one record");
		}
		Set<String> types = new HashSet<>();
		for (String recordId : selectedRecordIds) {
			Record record = recordServices.getDocumentById(recordId);
			Metadata typeMetadata = schemas.getRecordTypeMetadataOf(record);
			String type = record.get(typeMetadata);
			if (type == null) {
				return null;
			} else {
				types.add(type);
			}
		}
		return types.size() == 1 ? types.iterator().next() : null;
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

	public RecordVO newRecordVO(String schemaCode, final SessionContext sessionContext, final List<String> selectedRecordIds) {
		final MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(schemaCode);
		Record tmpRecord = modelLayerFactory.newRecordServices().newRecordWithSchema(schema);

		final Map<String, String> customizedLabels = getCustomizedLabels(schemaCode, locale);
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder() {
			@Override
			protected MetadataToVOBuilder newMetadataToVOBuilder() {
				return new MetadataToVOBuilder() {
					@Override
					protected MetadataVO newMetadataVO(String metadataCode, String datastoreCode,
							MetadataValueType type, String collection, MetadataSchemaVO schemaVO, boolean required,
							boolean multivalue, boolean readOnly, boolean unmodifiable,
							Map<Locale, String> labels, Class<? extends Enum<?>> enumClass, String[] taxonomyCodes,
							String schemaTypeCode, MetadataInputType metadataInputType,
							MetadataDisplayType metadataDisplayType, AllowedReferences allowedReferences,
							boolean enabled, StructureFactory structureFactory, String metadataGroup,
							Object defaultValue, String inputMask, Set<String> customAttributes) {
						// Replace labels with customized labels
						String customizedLabel = customizedLabels.get(metadataCode);
						if (customizedLabel != null) {
							for (Locale locale : labels.keySet()) {
								labels.put(locale, customizedLabel);
							}
						}
						// Default value is always null
						required = false;
						defaultValue = null;
						User user = schemas.getUser(sessionContext.getCurrentUser().getId());
						return isMetadataModifiable(metadataCode, user, selectedRecordIds) ?
								super.newMetadataVO(metadataCode, datastoreCode, type, collection, schemaVO, required, multivalue,
										readOnly,
										unmodifiable, labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType,
										metadataDisplayType,
										allowedReferences,
										enabled, structureFactory, metadataGroup, defaultValue, inputMask, customAttributes) :
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

	public BatchProcessResults execute(String selectedType, List<String> records, RecordVO viewObject, User user)
			throws RecordServicesException {
		BatchProcessAction batchProcessAction = toAction(selectedType, viewObject);
		BatchProcessRequest request;
		if (records != null && records.size() > 1000) {
			request = toRequest(selectedType, records.subList(0, 1000), viewObject, user);
		} else {
			request = toRequest(selectedType, records, viewObject, user);
		}

		return execute(request, batchProcessAction, records, user.getUsername(), "userBatchProcess");
	}

	public BatchProcessResults execute(BatchProcessRequest request, BatchProcessAction action, List<String> records,
			String username, String title)
			throws RecordServicesException {

		//		System.out.println("**************** EXECUTE ****************");
		//		System.out.println("ACTION : ");
		//		System.out.println(action);
		Transaction transaction = prepareTransaction(request, true);
		recordServices.validateTransaction(transaction);

		AsyncTask asyncTask = new ChangeValueOfMetadataBatchAsyncTask(request.getModifiedMetadatas(), null, records);
		AsyncTaskCreationRequest asyncTaskRequest = new AsyncTaskCreationRequest(asyncTask, collection, title);
		asyncTaskRequest.setUsername(username);

		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		batchProcessesManager.addAsyncTask(asyncTaskRequest);

		return null;
	}

	public BatchProcessResults simulate(String selectedType, List<String> records, RecordVO viewObject, User user)
			throws RecordServicesException {
		BatchProcessRequest request;
		if (records != null && records.size() > 100) {
			request = toRequest(selectedType, records.subList(0, 100), viewObject, user);
		} else {
			request = toRequest(selectedType, records, viewObject, user);
		}
		return simulateWithIds(request);
	}

	public BatchProcessResults simulateWithIds(BatchProcessRequest request)
			throws RecordServicesException.ValidationException {
		System.out.println("**************** SIMULATE ****************");
		System.out.println("REQUEST : ");
		System.out.println(request);
		Transaction transaction = prepareTransaction(request, true);
		recordServices.validateTransaction(transaction);
		BatchProcessResults results = toBatchProcessResults(transaction);

		System.out.println("\nRESULTS : ");
		System.out.println(results);
		return results;
	}

	public RecordVO newRecordVO(String schemaCode, final SessionContext sessionContext, final LogicalSearchQuery query) {
		final MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(schemaCode);
		Record tmpRecord = modelLayerFactory.newRecordServices().newRecordWithSchema(schema);

		final Map<String, String> customizedLabels = getCustomizedLabels(schemaCode, locale);
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder() {
			@Override
			protected MetadataToVOBuilder newMetadataToVOBuilder() {
				return new MetadataToVOBuilder() {
					@Override
					protected MetadataVO newMetadataVO(String metadataCode, String datastoreCode,
							MetadataValueType type, String collection, MetadataSchemaVO schemaVO, boolean required,
							boolean multivalue, boolean readOnly, boolean unmodifiable,
							Map<Locale, String> labels, Class<? extends Enum<?>> enumClass, String[] taxonomyCodes,
							String schemaTypeCode, MetadataInputType metadataInputType,
							MetadataDisplayType metadataDisplayType, AllowedReferences allowedReferences,
							boolean enabled, StructureFactory structureFactory, String metadataGroup,
							Object defaultValue, String inputMask, Set<String> customAttributes) {
						// Replace labels with customized labels
						String customizedLabel = customizedLabels.get(metadataCode);
						if (customizedLabel != null) {
							for (Locale locale : labels.keySet()) {
								labels.put(locale, customizedLabel);
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
						return super.newMetadataVO(metadataCode, datastoreCode, type, collection, schemaVO, required, multivalue,
								readOnly,
								unmodifiable, labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType,
								metadataDisplayType,
								allowedReferences,
								enabled, structureFactory, metadataGroup, defaultValue, inputMask, customAttributes);
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

	public BatchProcessResults execute(String selectedType, LogicalSearchQuery query, RecordVO viewObject, User user)
			throws RecordServicesException {
		LogicalSearchQuery validationQuery = query;
		validationQuery = validationQuery.setNumberOfRows(1000);
		BatchProcessAction batchProcessAction = toAction(selectedType, viewObject);
		BatchProcessRequest request = toRequest(selectedType, validationQuery, viewObject, user);
		return execute(request, batchProcessAction, query, user.getUsername(), "userBatchProcess");

	}

	public BatchProcessResults execute(BatchProcessRequest request, BatchProcessAction action, LogicalSearchQuery query,
			String username, String title)
			throws RecordServicesException {

		//		System.out.println("**************** EXECUTE ****************");
		//		System.out.println("ACTION : ");
		//		System.out.println(action);
		List<Transaction> transactionList = prepareTransactions(request, true);

		for (Transaction transaction : transactionList) {
			recordServices.validateTransaction(transaction);
		}

		AsyncTask asyncTask = new ChangeValueOfMetadataBatchAsyncTask(request.getModifiedMetadatas(), toQueryString(query), null);
		AsyncTaskCreationRequest asyncTaskRequest = new AsyncTaskCreationRequest(asyncTask, collection, title);
		asyncTaskRequest.setUsername(username);

		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		batchProcessesManager.addAsyncTask(asyncTaskRequest);

		return null;
	}

	private String toQueryString(LogicalSearchQuery query) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		ModifiableSolrParams params = searchServices.addSolrModifiableParams(query);
		String solrQuery = SolrUtils.toSingleQueryString(params);
		return solrQuery;
	}

	public BatchProcessResults simulate(String selectedType, LogicalSearchQuery query, RecordVO viewObject, User user)
			throws RecordServicesException {
		BatchProcessRequest request = toRequest(selectedType, query, viewObject, user);
		return simulateWithQuery(request);
	}

	public BatchProcessResults simulateWithQuery(BatchProcessRequest request)
			throws RecordServicesException.ValidationException {
		System.out.println("**************** SIMULATE ****************");
		System.out.println("REQUEST : ");
		System.out.println(request);
		List<Transaction> transactionList = prepareTransactions(request, true);

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

	public BatchProcessingMode getBatchProcessingMode() {
		ConstellioEIMConfigs eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		return eimConfigs.getBatchProcessingMode();
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
				transaction.addAllRecordsToReindex(response.getRecordsToReindex());
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

			for (Metadata metadata : types.getSchema(record.getSchemaCode()).getLazyTransientMetadatas()) {
				if (!LangUtils.isEqual(record.get(metadata), originalRecord.get(metadata))) {
					if (!Schemas.isGlobalMetadataExceptTitle(metadata.getLocalCode()) && extensions
							.isMetadataDisplayedWhenModifiedInBatchProcessing(metadata)) {
						modifiedMetadatas.add(metadata);
					}
				}
			}

			for (Metadata metadata : types.getSchema(record.getSchemaCode()).getEagerTransientMetadatas()) {
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
			transaction.addAllRecordsToReindex(modificationImpactCalculatorResponse.getRecordsToReindex());
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

	public List<Transaction> prepareTransactions(final BatchProcessRequest request, boolean recalculate) {

		final MetadataSchemaTypes types = schemas.getTypes();
		final List<Transaction> transactionList = new ArrayList<>();
		Transaction transaction = new Transaction();
		int counter = 0;

		List<Record> recordList = searchServices.search(request.getQuery());
		for (Record record : recordList) {
			transaction.add(record);
			if (++counter == 1000) {
				counter = 0;
				transactionList.add(transaction);
				transaction = new Transaction();
			}
			MetadataSchema currentRecordSchema = types.getSchema(record.getSchemaCode());

			for (Map.Entry<String, Object> entry : request.getModifiedMetadatas().entrySet()) {
				String localMetadataCode = new SchemaUtils().getLocalCodeFromMetadataCode(entry.getKey());
				String metadataCode = currentRecordSchema.getCode() + "_" + localMetadataCode;
				if (currentRecordSchema.hasMetadataWithCode(metadataCode)) {
					Metadata metadata = currentRecordSchema.get(metadataCode);

					if (isNonEmptyValue(metadata, entry.getValue()) && types.isRecordTypeMetadata(metadata)) {
						record.set(metadata, entry.getValue());
						changeSchemaTypeAccordingToTypeLinkedSchema(record, types, new RecordProvider(recordServices), metadata);
					}
				}
			}

			currentRecordSchema = types.getSchema(record.getSchemaCode());
			for (Map.Entry<String, Object> entry : request.getModifiedMetadatas().entrySet()) {
				String localMetadataCode = new SchemaUtils().getLocalCodeFromMetadataCode(entry.getKey());

				String metadataCode = currentRecordSchema.getCode() + "_" + localMetadataCode;
				if (currentRecordSchema.hasMetadataWithCode(metadataCode)) {
					Metadata metadata = currentRecordSchema.get(currentRecordSchema.getCode() + "_" + localMetadataCode);
					if (isNonEmptyValue(metadata, entry.getValue())) {
						record.set(metadata, entry.getValue());
					}
				}
			}
		}
		if (counter < 1000) {
			transactionList.add(transaction);
		}

		return transactionList;
	}

	public Transaction prepareTransaction(BatchProcessRequest request, boolean recalculate) {
		Transaction transaction = new Transaction();
		MetadataSchemaTypes types = schemas.getTypes();
		for (String id : request.getIds()) {
			Record record = recordServices.getDocumentById(id);
			transaction.add(record);
			MetadataSchema currentRecordSchema = types.getSchema(record.getSchemaCode());

			for (Map.Entry<String, Object> entry : request.getModifiedMetadatas().entrySet()) {
				String localMetadataCode = new SchemaUtils().getLocalCodeFromMetadataCode(entry.getKey());
				String metadataCode = currentRecordSchema.getCode() + "_" + localMetadataCode;
				if (currentRecordSchema.hasMetadataWithCode(metadataCode)) {
					Metadata metadata = currentRecordSchema.get(metadataCode);

					if (isNonEmptyValue(metadata, entry.getValue()) && types.isRecordTypeMetadata(metadata)) {
						record.set(metadata, entry.getValue());
						changeSchemaTypeAccordingToTypeLinkedSchema(record, types, new RecordProvider(recordServices), metadata);
					}
				}
			}

			currentRecordSchema = types.getSchema(record.getSchemaCode());
			for (Map.Entry<String, Object> entry : request.getModifiedMetadatas().entrySet()) {
				String localMetadataCode = new SchemaUtils().getLocalCodeFromMetadataCode(entry.getKey());

				String metadataCode = currentRecordSchema.getCode() + "_" + localMetadataCode;
				if (currentRecordSchema.hasMetadataWithCode(metadataCode)) {
					Metadata metadata = currentRecordSchema.get(currentRecordSchema.getCode() + "_" + localMetadataCode);
					if (isNonEmptyValue(metadata, entry.getValue())) {
						record.set(metadata, entry.getValue());
					}
				}
			}

		}

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

	public String getSchema(String schemaType, String typeId) {
		if (StringUtils.isBlank(typeId)) {
			return schemaType + "_default";
		}
		Record record = recordServices.getDocumentById(typeId);
		return schemas.getLinkedSchemaOf(record);
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
		return schemas.getRecordTypeMetadataOf(schemas.getTypes().getSchemaType(schemaType)).getReferencedSchemaType();
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
		params.setSelectedTypeId(selectedType);

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

	public RecordFieldFactory newRecordFieldFactory(String schemaType, String selectedType, List<String> selectedRecordIds) {
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

	public BatchProcessRequest toRequest(String selectedType, LogicalSearchQuery query, RecordVO formVO, User user) {

		String typeCode = new SchemaUtils().getSchemaTypeCode(formVO.getSchema().getCode());
		MetadataSchemaType type = schemas.getTypes().getSchemaType(typeCode);
		MetadataSchema schema = schemas.getTypes().getSchema(formVO.getSchema().getCode());
		Map<String, Object> fieldsModifications = new HashMap<>();
		for (MetadataVO metadataVO : formVO.getMetadatas()) {
			Metadata metadata = schema.get(metadataVO.getLocalCode());
			Object value = formVO.get(metadataVO);

			LOGGER.info(metadata.getCode() + ":" + value);
			if (metadata.getDataEntry().getType() == DataEntryType.MANUAL
					&& value != null
					&& (!metadata.isSystemReserved() || Schemas.TITLE_CODE.equals(metadata.getLocalCode()))
					&& (!metadata.isMultivalue() || !((List) value).isEmpty())
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

		query.setPreferAnalyzedFields(true);
		return new BatchProcessRequest(null, query, user, type, fieldsModifications);
	}

	public BatchProcessRequest toRequest(String selectedType, List<String> selectedRecord, RecordVO formVO, User user) {

		String typeCode = new SchemaUtils().getSchemaTypeCode(formVO.getSchema().getCode());
		MetadataSchemaType type = schemas.getTypes().getSchemaType(typeCode);
		MetadataSchema schema = schemas.getTypes().getSchema(formVO.getSchema().getCode());
		Map<String, Object> fieldsModifications = new HashMap<>();
		for (MetadataVO metadataVO : formVO.getMetadatas()) {
			Metadata metadata = schema.get(metadataVO.getLocalCode());
			Object value = formVO.get(metadataVO);

			LOGGER.info(metadata.getCode() + ":" + value);
			if (metadata.getDataEntry().getType() == DataEntryType.MANUAL
					&& value != null
					&& (!metadata.isSystemReserved() || Schemas.TITLE_CODE.equals(metadata.getLocalCode()))
					&& (!metadata.isMultivalue() || !((List) value).isEmpty())
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

		return new BatchProcessRequest(selectedRecord, null, user, type, fieldsModifications);
	}

	public BatchProcessAction toAction(String selectedType, RecordVO formVO) {

		String typeCode = new SchemaUtils().getSchemaTypeCode(formVO.getSchema().getCode());
		MetadataSchemaType type = schemas.getTypes().getSchemaType(typeCode);
		MetadataSchema schema = schemas.getTypes().getSchema(formVO.getSchema().getCode());
		Map<String, Object> fieldsModifications = new HashMap<>();
		for (MetadataVO metadataVO : formVO.getMetadatas()) {
			Metadata metadata = schema.get(metadataVO.getLocalCode());
			Object value = formVO.get(metadataVO);

			LOGGER.info(metadata.getCode() + ":" + value);
			if (metadata.getDataEntry().getType() == DataEntryType.MANUAL
					&& value != null
					&& (!metadata.isSystemReserved() || Schemas.TITLE_CODE.equals(metadata.getLocalCode()))
					&& (!metadata.isMultivalue() || !((List) value).isEmpty())
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
			new BatchProcessingResultReportWriter(new BatchProcessingResultModel(results, locale), i18n.getLocale())
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

