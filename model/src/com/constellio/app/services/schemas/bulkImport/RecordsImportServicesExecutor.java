package com.constellio.app.services.schemas.bulkImport;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.services.schemas.bulkImport.BulkImportParams.ImportErrorsBehavior;
import com.constellio.app.services.schemas.bulkImport.BulkImportParams.ImportValidationErrorsBehavior;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils.StringReplacer;
import com.constellio.data.utils.ThreadUtils.IteratorElementTask;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchSchemaType;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.structures.*;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.frameworks.validation.DecoratedValidationsErrors;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.*;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.*;
import com.constellio.model.services.records.bulkImport.ProgressionHandler;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.validators.MaskedMetadataValidator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.app.services.schemas.bulkImport.BulkImportParams.ImportErrorsBehavior.CONTINUE_FOR_RECORD_OF_SAME_TYPE;
import static com.constellio.app.services.schemas.bulkImport.BulkImportParams.ImportErrorsBehavior.STOP_ON_FIRST_ERROR;
import static com.constellio.app.services.schemas.bulkImport.Resolver.toResolver;
import static com.constellio.data.utils.LangUtils.replacingLiteral;
import static com.constellio.data.utils.ThreadUtils.iterateOverRunningTaskInParallel;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.*;

public class RecordsImportServicesExecutor {

	public static StringReplacer IMPORTED_FILEPATH_CLEANER = replacingLiteral("/", separator).replacingLiteral("\\", "/");
	public static final String INVALID_SCHEMA_TYPE_CODE = "invalidSchemaTypeCode";
	public static final String LEGACY_ID_LOCAL_CODE = LEGACY_ID.getLocalCode();
	static final List<String> ALL_BOOLEAN_YES = asList("o", "y", "t", "oui", "vrai", "yes", "true", "1");
	static final List<String> ALL_BOOLEAN_NO = asList("n", "f", "non", "faux", "no", "false", "0");
	private static final String IMPORT_URL_INPUTSTREAM_NAME = "RecordsImportServices-ImportURL";
	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsImportServices.class);
	private static final String CYCLIC_DEPENDENCIES_ERROR = "cyclicDependencies";
	private static final String CONTENT_NOT_FOUND_ERROR = "contentNotFound";
	private static final String HASH_NOT_FOUND_IN_VAULT = "hashNotFoundInVault";
	private static final String CONTENT_NOT_IMPORTED_ERROR = "contentNotImported";
	private static final String RECORD_PREPARATION_ERROR = "recordPreparationError";

	public static final String RECORD_SERVICE_EXEPTION = "recordServiceException";

	public static final String COMMENT_MESSAGE = "message";
	public static final String COMMENT_USER_NAME = "userName";
	public static final String COMMENT_DATE_TIME = "dateTime";

	public static final String EMAIL_ADDRESS_EMAIL = "Email";
	public static final String EMAIL_ADDRESS_NAME = "Name";

	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemasManager schemasManager;
	private RecordServices recordServices;
	private ContentManager contentManager;
	private IOServices ioServices;
	private SearchServices searchServices;
	private URLResolver urlResolver;
	private final ImportDataProvider importDataProvider;
	private final BulkImportProgressionListener bulkImportProgressionListener;
	private final User user;
	private final List<String> collections;
	private final BulkImportParams params;
	private final ProgressionHandler progressionHandler;
	private final String collection;
	private ModelLayerCollectionExtensions extensions;
	private MetadataSchemaTypes types;
	private Language language;
	private BulkImportResults importResults;
	private Map<String, Factory<ContentVersionDataSummary>> importedFilesMap;
	private SkippedRecordsImport skippedRecordsImport;
	private ConfigProvider configProvider;
	ResolverCache resolverCache;
	SchemasRecordsServices schemasRecordsServices;

	private static class TypeImportContext {
		List<String> uniqueMetadatas;
		String schemaType;
		AtomicInteger addUpdateCount;
		boolean recordsBeforeImport;
		boolean hasContents;
	}

	private static class TypeBatchImportContext {
		List<ImportData> batch;
		Transaction transaction;
		ImportDataOptions options;
		BulkUploader bulkUploader;
		Map<String, Long> sequences = new HashMap<>();
		TypeImportContext typeImportContext;

		public TypeBatchImportContext(
				TypeImportContext typeImportContext) {
			this.typeImportContext = typeImportContext;
		}
	}

	//
	public RecordsImportServicesExecutor(final ModelLayerFactory modelLayerFactory, RecordServices recordServices,
										 URLResolver urlResolver,
										 ImportDataProvider importDataProvider,
										 final BulkImportProgressionListener bulkImportProgressionListener,
										 final User user, List<String> collections, BulkImportParams params) {
		this.modelLayerFactory = modelLayerFactory;
		this.importDataProvider = importDataProvider;
		this.bulkImportProgressionListener = bulkImportProgressionListener;
		this.user = user;
		this.collections = collections;
		this.params = params;
		this.recordServices = recordServices;
		schemasManager = modelLayerFactory.getMetadataSchemasManager();

		searchServices = modelLayerFactory.newSearchServices();
		contentManager = modelLayerFactory.getContentManager();
		ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		this.skippedRecordsImport = new SkippedRecordsImport();
		this.urlResolver = urlResolver;
		this.progressionHandler = new ProgressionHandler(bulkImportProgressionListener);
		this.collection = user.getCollection();
		this.importResults = new BulkImportResults();
		this.configProvider = new ConfigProvider() {

			@Override
			public <T> T get(SystemConfiguration config) {
				return modelLayerFactory.getSystemConfigurationsManager().getValue(config);
			}
		};

		schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);
	}

	public BulkImportResults bulkImport()
			throws RecordsImportServicesRuntimeException, ValidationException {
		ValidationErrors errors = new ValidationErrors();
		try {
			initialize();
			validate(errors);
			return run(errors);
		} finally {
			importDataProvider.close();
		}
	}

	RecordsImportServicesExecutor initialize() {
		language = Language.withCode(user.getLoginLanguageCode());
		if (language == null) {
			language = Language.withCode(modelLayerFactory.getConfiguration().getMainDataLanguage());
		}

		new RecordCachesServices(modelLayerFactory).loadCachesIn(collection);
		importDataProvider.initialize();
		extensions = modelLayerFactory.getExtensions().forCollection(collection);
		types = schemasManager.getSchemaTypes(collection);
		resolverCache = new ResolverCache(modelLayerFactory.newRecordServices(), searchServices, types, importDataProvider);

		return this;
	}

	private BulkImportResults run(ValidationErrors errors)
			throws ValidationException {

		importedFilesMap = new HashMap<>();
		for (Map.Entry<String, Factory<ContentVersionDataSummary>> entry : contentManager.getImportedFilesMap().entrySet()) {
			String key = entry.getKey().contains("[\\]") ? entry.getKey().replace("\\", "/") : entry.getKey();
			importedFilesMap.put(key, entry.getValue());
		}
		for (String schemaType : getImportedSchemaTypes()) {

			DecoratedValidationsErrors typeImportErrors = new DecoratedValidationsErrors(errors);
			TypeImportContext context = new TypeImportContext();
			context.schemaType = schemaType;
			context.recordsBeforeImport = searchServices.hasResults(from(types.getSchemaType(schemaType)).returnAll());
			progressionHandler.beforeImportOf(schemaType);
			context.uniqueMetadatas = types.getSchemaType(schemaType).getAllMetadatas().onlyWithType(STRING).onlyUniques()
					.toLocalCodesList();
			int previouslySkipped = 0;
			context.addUpdateCount = new AtomicInteger();
			context.hasContents = !types.getSchemaType(schemaType).getAllMetadatas().onlyWithType(CONTENT).isEmpty();
			boolean typeImportFinished = false;

			while (!typeImportFinished) {

				ImportDataIterator importDataIterator = importDataProvider.newDataIterator(schemaType);
				try {
					int skipped;
					if (params.getThreads() == 1) {
						skipped = bulkImport(context, importDataIterator, typeImportErrors);
					} else {
						skipped = bulkImportInParallel(context, importDataIterator, typeImportErrors);
					}
					if (skipped > 0 && skipped == previouslySkipped) {

						if (!typeImportErrors.hasDecoratedErrors()) {
							Set<String> cyclicDependentIds = resolverCache
									.getNotYetImportedLegacyIds(schemaType, importDataIterator.getOptions().isImportAsLegacyId());
							addCyclicDependenciesValidationError(typeImportErrors, types.getSchemaType(schemaType),
									cyclicDependentIds);

						}

						throwIfNonEmpty(typeImportErrors);
					}
					if (skipped == 0) {
						typeImportFinished = true;
					}
					previouslySkipped = skipped;
				} finally {
					importDataIterator.close();
				}
			}
			if (params.importErrorsBehavior == STOP_ON_FIRST_ERROR
				|| params.importErrorsBehavior == CONTINUE_FOR_RECORD_OF_SAME_TYPE) {
				if (typeImportErrors.hasDecoratedErrors()) {
					throwIfNonEmpty(typeImportErrors);
				}
			}
		}

		progressionHandler.onImportFinished();
		throwIfNonEmptyErrorOrWarnings(errors);

		return importResults;
	}

	private void throwIfNonEmptyErrorOrWarnings(ValidationErrors errors)
			throws ValidationException {
		if (!errors.isEmptyErrorAndWarnings()) {
			skippedRecordsImport.addWarningForSkippedRecordsBecauseOfDependencies(language, types, errors);
			errors.throwIfNonEmptyErrorOrWarnings();
		}
	}

	private void throwIfNonEmpty(ValidationErrors errors)
			throws ValidationException {
		if (!errors.isEmpty()) {
			skippedRecordsImport.addWarningForSkippedRecordsBecauseOfDependencies(language, types, errors);
			errors.throwIfNonEmpty();
		}
	}

	private void addCyclicDependenciesValidationError(ValidationErrors errors, MetadataSchemaType schemaType,
													  Set<String> cyclicDependentIds) {

		List<String> ids = new ArrayList<>(cyclicDependentIds);
		Collections.sort(ids);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("prefix", schemaType.getLabel(language) + " : ");

		parameters.put("cyclicDependentIds", StringUtils.join(ids, ", "));
		parameters.put("schemaType", schemaType.getCode());

		errors.add(RecordsImportServices.class, CYCLIC_DEPENDENCIES_ERROR, parameters);
	}

	int bulkImport(TypeImportContext typeImportContext, ImportDataIterator importDataIterator, ValidationErrors errors)
			throws ValidationException {

		int skipped = 0;
		Iterator<List<ImportData>> importDataBatches = new BatchBuilderIterator<>(importDataIterator, params.getBatchSize());
		while (importDataBatches.hasNext()) {
			try {
				TypeBatchImportContext typeBatchImportContext = newTypeBatchImportContext(typeImportContext,
						importDataIterator.getOptions(), importDataBatches.next());
				skipped += importBatch(typeImportContext, typeBatchImportContext, errors);
				typeBatchImportContext.transaction.getRecordUpdateOptions().setSkipFindingRecordsToReindex(true);
				recordServices.executeHandlingImpactsAsync(typeBatchImportContext.transaction);
				incrementSequences(typeBatchImportContext);

			} catch (RecordServicesException e) {
				while (importDataBatches.hasNext()) {
					importDataBatches.next();
				}
				throw new RuntimeException(e);
			}

		}

		return skipped;
	}

	private void incrementSequences(TypeBatchImportContext typeBatchImportContext) {
		SequencesManager sequencesManager = modelLayerFactory.getDataLayerFactory().getSequencesManager();
		for (Map.Entry<String, Long> highestSequenceValue : typeBatchImportContext.sequences.entrySet()) {
			long currentSystemValue = sequencesManager.getLastSequenceValue(highestSequenceValue.getKey());
			if (currentSystemValue < highestSequenceValue.getValue()) {
				sequencesManager.set(highestSequenceValue.getKey(), highestSequenceValue.getValue());
			}
		}
	}

	private TypeBatchImportContext newTypeBatchImportContext(TypeImportContext typeImportContext,
															 ImportDataOptions options, List<ImportData> batch) {
		TypeBatchImportContext typeBatchImportContext = new TypeBatchImportContext(typeImportContext);
		typeBatchImportContext.batch = batch;
		typeBatchImportContext.transaction = new Transaction();
		typeBatchImportContext.transaction.getRecordUpdateOptions().setSkipReferenceValidation(true);
		typeBatchImportContext.transaction.setSkippingReferenceToLogicallyDeletedValidation(true);
		typeBatchImportContext.transaction.getRecordUpdateOptions().setSkipMaskedMetadataValidations(true);
		typeBatchImportContext.transaction.getRecordUpdateOptions().setSkipUSRMetadatasRequirementValidations(
				params.isWarningsForRequiredUSRMetadatasWithoutValue());
		typeBatchImportContext.options = options;
		if (!typeImportContext.recordsBeforeImport) {
			typeBatchImportContext.transaction.getRecordUpdateOptions().setUnicityValidationsEnabled(false);
		}
		return typeBatchImportContext;
	}

	int bulkImportInParallel(final TypeImportContext typeImportContext, ImportDataIterator importDataIterator,
							 final ValidationErrors errors)
			throws ValidationException {

		final AtomicInteger skipped = new AtomicInteger();

		int batchSize = params.getBatchSize();
		int threads = params.getThreads();

		if (typeImportContext.hasContents) {
			batchSize = (int) Math.ceil(batchSize / 2.0);
			threads = 1;//(int) Math.ceil(threads / 4.0);
		}

		Iterator<List<ImportData>> importDataBatches = new BatchBuilderIterator<>(importDataIterator, batchSize);
		final ImportDataOptions options = importDataIterator.getOptions();
		try {
			iterateOverRunningTaskInParallel(importDataBatches, threads, new IteratorElementTask<List<ImportData>>() {
				@Override
				public void executeTask(List<ImportData> value)
						throws Exception {
					TypeBatchImportContext typeBatchImportContext = newTypeBatchImportContext(typeImportContext, options, value);
					skipped.addAndGet(importBatch(typeImportContext, typeBatchImportContext, errors));
					typeBatchImportContext.transaction.getRecordUpdateOptions().setSkipFindingRecordsToReindex(true);
					recordServices.executeHandlingImpactsAsync(typeBatchImportContext.transaction);

				}
			});
		} catch (ValidationException e) {
			throw e;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		while (importDataIterator.hasNext()) {
			importDataIterator.next();
		}

		return skipped.get();
	}

	private int importBatch(final TypeImportContext typeImportContext, TypeBatchImportContext typeBatchImportContext,
							ValidationErrors errors)
			throws ValidationException {

		preuploadContents(typeBatchImportContext);

		int skipped = 0;
		int errorsCount = 0;
		List<ImportData> batch = typeBatchImportContext.batch;
		for (int i = 0; i < batch.size(); i++) {
			final ImportData toImport = batch.get(i);
			final String schemaTypeLabel = types.getSchemaType(typeImportContext.schemaType).getLabel(language);

			DecoratedValidationsErrors decoratedValidationsErrors = new DecoratedValidationsErrors(errors) {
				@Override
				public void buildExtraParams(Map<String, Object> parameters) {
					if (toImport.getValue("code") != null) {
						parameters.put("prefix", schemaTypeLabel + " " + toImport.getValue("code") + " : ");
					} else {
						parameters.put("prefix", schemaTypeLabel + " " + toImport.getLegacyId() + " : ");
					}

					parameters.put("index", "" + (toImport.getIndex() + 1));
					parameters.put("legacyId", toImport.getLegacyId());
					parameters.put("schemaType", typeImportContext.schemaType);
				}
			};
			String legacyId = toImport.getLegacyId();
			try {

				if (!this.skippedRecordsImport.isSkipped(typeImportContext.schemaType, legacyId)) {
					importRecord(typeImportContext, typeBatchImportContext, toImport, decoratedValidationsErrors);
				}

			} catch (PostponedRecordException e) {
				progressionHandler.onRecordImportPostponed(legacyId);
				skipped++;
			}

			if (decoratedValidationsErrors.hasDecoratedErrors()) {
				errorsCount++;
			}

		}

		//		LOGGER.info("Splitter cache object count : " + SchemaUtils.underscoreSplitCache.size());
		//		LOGGER.info("Record cache object count : " + modelLayerFactory.getRecordsCaches().getCacheObjectsCount());
		//		LOGGER.info("Import cache object count : " + resolverCache.getCacheTotalSize());

		String firstId = batch.get(0).getLegacyId();
		String lastId = batch.get(batch.size() - 1).getLegacyId();

		progressionHandler.afterRecordImports(firstId, lastId, batch.size(), errorsCount);

		if (modelLayerFactory.getConfiguration().isDeleteUnusedContentEnabled()) {
			contentManager.deleteUnreferencedContents(RecordsFlushing.NOW());
		}
		return skipped;
	}

	private void importRecord(TypeImportContext typeImportContext, TypeBatchImportContext typeBatchImportContext,
							  ImportData toImport, DecoratedValidationsErrors errors)
			throws ValidationException, PostponedRecordException {

		String legacyId = toImport.getLegacyId();
		//		if(typeImportContext.schemaType.equals("document")) {
		//			if(((ContentImport) toImport.getFields().get("content")).getFileName().equals("wiki-49.bigf/Jorj_Robin_3a1a.html")) {
		//				System.out.println("test");
		//			}
		//		}
		if (resolverCache
				.getNotYetImportedLegacyIds(typeImportContext.schemaType, typeBatchImportContext.options.isImportAsLegacyId())
				.contains(legacyId)) {

			extensions.callRecordImportValidate(typeImportContext.schemaType,
					new ValidationParams(errors, toImport, typeBatchImportContext.options,
							params.isWarningsForInvalidFacultativeMetadatas()));

			String title = (String) toImport.getFields().get("title");

			try {
				Record record = buildRecord(typeImportContext, typeBatchImportContext, toImport, errors);

				typeBatchImportContext.transaction.add(record);
				typeImportContext.addUpdateCount.incrementAndGet();

				try {
					recordServices.validateRecordInTransaction(record, typeBatchImportContext.transaction);

				} catch (ContentManagerRuntimeException_NoSuchContent e) {
					e.printStackTrace();
					Map<String, Object> params = new HashMap<>();
					params.put("hash", e.getId());
					errors.add(RecordsImportServices.class, HASH_NOT_FOUND_IN_VAULT, params);
				} catch (RecordServicesException.ValidationException e) {
					for (ValidationError error : e.getErrors().getValidationErrors()) {
						errors.add(error, error.getParameters());
					}

				} catch (Throwable t) {
					String stack = ExceptionUtils.getStackTrace(t);
					Map<String, Object> params = new HashMap<>();
					params.put("message", t.getMessage());
					params.put("stacktrace", stack);
					errors.add(RecordsImportServices.class, RECORD_PREPARATION_ERROR, params);

				}

				if (errors.hasDecoratedErrors()) {
					if (params.getImportErrorsBehavior() == ImportErrorsBehavior.STOP_ON_FIRST_ERROR) {
						throw new ValidationException(errors);

					} else {
						skippedRecordsImport.markAsSkippedBecauseOfFailure(typeImportContext.schemaType, legacyId);
						typeBatchImportContext.transaction.remove(record);
					}

				} else {
					if (typeBatchImportContext.options.isImportAsLegacyId()) {
						resolverCache.mapIds(typeImportContext.schemaType, LEGACY_ID_LOCAL_CODE, legacyId, record.getId());
					} else {
						resolverCache.mapIds(typeImportContext.schemaType, Schemas.IDENTIFIER.getLocalCode(), legacyId,
								record.getId());
					}

					for (String uniqueMetadata : typeImportContext.uniqueMetadatas) {
						String value = (String) toImport.getFields().get(uniqueMetadata);
						if (value != null) {
							resolverCache.mapIds(typeImportContext.schemaType, uniqueMetadata, value, record.getId());
						}
					}

					findHigherSequenceValues(typeImportContext, typeBatchImportContext, record);
				}

			} catch (SkippedBecauseOfFailedDependency e) {
				skippedRecordsImport.markAsSkippedBecauseOfDependencyFailure(typeImportContext.schemaType, legacyId);
			}
			progressionHandler.incrementProgression();
		}
	}

	private void findHigherSequenceValues(TypeImportContext typeImportContext,
										  TypeBatchImportContext typeBatchImportContext,
										  Record record) {

		MetadataSchema schema = types.getSchema(record.getSchemaCode());
		for (Metadata metadata : schema.getMetadatas().onlySequence()) {
			if (record.get(metadata) != null) {
				SequenceDataEntry sequenceDataEntry = (SequenceDataEntry) metadata.getDataEntry();
				String key = sequenceDataEntry.getFixedSequenceCode();
				if (key == null) {
					key = record.get(schema.getMetadata(sequenceDataEntry.getMetadataProvidingSequenceCode()));
				}

				long value = Long.valueOf(record.<String>get(metadata));
				Long currentHighestValue = typeBatchImportContext.sequences.get(key);
				if (isNotBlank(key) && (currentHighestValue == null || currentHighestValue.longValue() < value)) {
					typeBatchImportContext.sequences.put(key, value);
				}
			}
		}
	}

	private void preuploadContents(TypeBatchImportContext typeBatchImportContext) {
		List<Metadata> contentMetadatas = types.getAllContentMetadatas();
		if (!contentMetadatas.isEmpty()) {

			for (ImportData toImport : typeBatchImportContext.batch) {

				for (Metadata contentMetadata : contentMetadatas) {
					if (toImport.getFields().containsKey(contentMetadata.getLocalCode())) {
						List<ImportContent> contentImports = new ArrayList<>();
						if (contentMetadata.isMultivalue()) {
							contentImports.addAll((List) toImport.getFields().get(contentMetadata.getLocalCode()));
						} else {
							contentImports.add((ImportContent) toImport.getFields().get(contentMetadata.getLocalCode()));

						}
						for (ImportContent contentImport : contentImports) {
							if (contentImport instanceof SimpleImportContent) {
								SimpleImportContent simpleImportContent = (SimpleImportContent) contentImport;
								for (Iterator<ContentImportVersion> iterator = simpleImportContent.getVersions()
										.iterator(); iterator.hasNext(); ) {
									ContentImportVersion version = iterator.next();
									String url = version.getUrl();
									if (!url.toLowerCase().startsWith("imported://")) {
										StreamFactory<InputStream> inputStreamStreamFactory = urlResolver
												.resolve(url, version.getFileName());

										if (typeBatchImportContext.bulkUploader == null) {
											typeBatchImportContext.bulkUploader = new BulkUploader(modelLayerFactory);
											typeBatchImportContext.bulkUploader.setHandleDeletionOfUnreferencedHashes(false);
										}

										if (iterator.hasNext()) {
											typeBatchImportContext.bulkUploader
													.uploadAsyncWithoutParsing(url, inputStreamStreamFactory,
															version.getFileName());
										} else {
											typeBatchImportContext.bulkUploader.uploadAsync(url, inputStreamStreamFactory);
										}
									}
								}
							}
						}
					}
				}

			}
			if (typeBatchImportContext.bulkUploader != null) {
				typeBatchImportContext.bulkUploader.close();
			}
		}
	}

	Record buildRecord(TypeImportContext typeImportContext, TypeBatchImportContext typeBatchImportContext,
					   ImportData toImport, ValidationErrors errors)
			throws PostponedRecordException, SkippedBecauseOfFailedDependency {
		MetadataSchemaType schemaType = getMetadataSchemaType(typeImportContext.schemaType);
		MetadataSchema newSchema = getMetadataSchema(typeImportContext.schemaType + "_" + toImport.getSchema());

		Record record;
		String legacyId = toImport.getLegacyId();
		boolean importAsLegacyId = typeBatchImportContext.options.isImportAsLegacyId();
		if (resolverCache.isRecordUpdate(typeImportContext.schemaType, legacyId, importAsLegacyId)) {
			if (importAsLegacyId) {
				record = modelLayerFactory.newSearchServices()
						.searchSingleResult(from(schemaType).where(LEGACY_ID).isEqualTo(legacyId));
			} else {
				record = modelLayerFactory.newSearchServices()
						.searchSingleResult(from(schemaType).where(Schemas.IDENTIFIER).isEqualTo(legacyId));
			}
		} else {
			record = null;
			if (typeBatchImportContext.options.isMergeExistingRecordWithSameUniqueMetadata()) {
				for (String uniqueMetadataCode : typeImportContext.uniqueMetadatas) {
					Metadata uniqueMetadata = newSchema.getMetadata(uniqueMetadataCode);

					record = recordServices.getRecordByMetadata(uniqueMetadata, (String) toImport.getValue(uniqueMetadataCode));
					if (record != null) {
						break;
					}
				}
			}

			if (record == null) {
				if (importAsLegacyId) {
					record = recordServices.newRecordWithSchema(newSchema);
				} else {
					try {
						record = recordServices.getDocumentById(legacyId);
						if (!record.isOfSchemaType(schemaType.getCode())) {
							record = recordServices.newRecordWithSchema(newSchema, legacyId);
						}
					} catch (Exception e) {
						record = recordServices.newRecordWithSchema(newSchema, legacyId);
					}
				}
			}
		}

		if (!newSchema.getCode().equals(record.getSchemaCode())) {
			MetadataSchema wasSchema = getMetadataSchema(record.getSchemaCode());
			record.changeSchema(wasSchema, newSchema);
		}
		record.set(LEGACY_ID, legacyId);
		for (Entry<String, Object> field : toImport.getFields().entrySet()) {

			String key = field.getKey();
			Locale locale = null;
			if (key.contains("_")) {
				locale = new Locale(substringAfter(key, "_"));
				key = substringBefore(key, "_");
			}
			Metadata metadata = newSchema.getMetadata(key);
			if (metadata.getType() != MetadataValueType.STRUCTURE) {
				Object value = field.getValue();
				Object convertedValue =
						value == null ? null : convertValue(typeBatchImportContext, metadata, value, errors);

				boolean setValue = true;

				DecoratedValidationsErrors decoratedErrors = new DecoratedValidationsErrors(new ValidationErrors());

				for (RecordMetadataValidator validator : metadata.getValidators()) {
					validator.validate(metadata, convertedValue, configProvider, decoratedErrors);
				}

				if (metadata.getInputMask() != null) {
					validateMask(metadata, convertedValue, decoratedErrors);
				}

				if (decoratedErrors.hasDecoratedErrorsOrWarnings()) {
					if (params.isWarningsForInvalidFacultativeMetadatas()) {
						setValue = false;
						errors.addAllWarnings(decoratedErrors.getValidationErrors());
						errors.addAllWarnings(decoratedErrors.getValidationWarnings());
					} else {
						errors.addAll(decoratedErrors.getValidationErrors());
						errors.addAllWarnings(decoratedErrors.getValidationWarnings());
					}
				}

				if (setValue) {
					record.set(metadata, locale, convertedValue);
				}

			} else {
				if (metadata.getStructureFactory().getClass().equals(MapStringListStringStructureFactory.class)) {
					manageMapStringListStringStructureFactory(record, metadata, field);
				} else if (metadata.getStructureFactory().getClass().equals(MapStringStringStructureFactory.class)) {
					manageMapStringStringStructureFactory(record, metadata, field);
				} else if (metadata.getStructureFactory().getClass().equals(CommentFactory.class)) {
					manageCommentFactory(record, metadata, field);
				} else if (metadata.getStructureFactory().getClass().equals(EmailAddressFactory.class)) {
					manageEmailAddressFactory(record, metadata, field);
				}
			}
		}

		extensions.callRecordImportBuild(typeImportContext.schemaType,
				new BuildParams(record, types, toImport, typeBatchImportContext.options,
						params.isAllowingReferencesToNonExistingUsers()));

		return record;
	}

	private void manageEmailAddressFactory(Record record, Metadata metadata, Entry<String, Object> field) {
		//Test if multi value
		if (metadata.isMultivalue()) {
			List<Map<String, String>> listHashMap = (List<Map<String, String>>) field.getValue();
			List<EmailAddress> emailAddressList = new ArrayList<>();
			for (Map<String, String> map : listHashMap) {
				EmailAddress emailAddress = new EmailAddress();

				emailAddress.setEmail(map.get(EMAIL_ADDRESS_EMAIL));
				emailAddress.setName(map.get(EMAIL_ADDRESS_NAME));
				emailAddressList.add(emailAddress);

			}
			record.set(metadata, emailAddressList);
		} else {

			Map<String, String> map = (Map<String, String>) field.getValue();
			EmailAddress emailAddress = new EmailAddress();

			emailAddress.setEmail(map.get(EMAIL_ADDRESS_EMAIL));
			emailAddress.setName(map.get(EMAIL_ADDRESS_NAME));

			record.set(metadata, emailAddress);
		}
	}

	private void manageCommentFactory(Record record, Metadata metadata, Entry<String, Object> field) {
		if (metadata.isMultivalue()) {
			List<Map<String, String>> listHashMap = (List<Map<String, String>>) field.getValue();
			List<Comment> commentList = new ArrayList<>();

			for (Map<String, String> hashMap : listHashMap) {
				String userName = hashMap.get(COMMENT_USER_NAME);
				UserServices userService = new UserServices(modelLayerFactory);

				Comment comment = new Comment();
				comment.setMessage(hashMap.get(COMMENT_MESSAGE));
				comment.setUser(userName == null ? null : userService.getUserInCollection(userName, collection));

				if (hashMap.get(COMMENT_DATE_TIME) != null) {
					comment.setDateTime(LocalDateTime.parse(hashMap.get(COMMENT_DATE_TIME)));
				}

				commentList.add(comment);
			}
			record.set(metadata, commentList);
		} else {
			Map<String, String> hashMap = (Map<String, String>) field.getValue();
			String userName = hashMap.get(COMMENT_USER_NAME);
			UserServices userService = new UserServices(modelLayerFactory);

			Comment comment = new Comment();
			comment.setMessage(hashMap.get(COMMENT_MESSAGE));
			comment.setUser(userService.getUserInCollection(userName, collection));

			if (comment.getDateTime() != null) {
				LocalDate.parse(hashMap.get(COMMENT_DATE_TIME));
			}

			record.set(metadata, comment);
		}
	}

	private void manageMapStringStringStructureFactory(Record record, Metadata metadata, Entry<String, Object> field) {

		if (metadata.isMultivalue()) {
			List<Map<String, String>> listHashMap = (List<Map<String, String>>) field.getValue();
			List<MapStringStringStructure> listMapStringListStringStructure = new ArrayList<>();

			for (Map<String, String> map : listHashMap) {
				MapStringStringStructure mapStringStringStructure = new MapStringStringStructure(map);
				listMapStringListStringStructure.add(mapStringStringStructure);
			}
			record.set(metadata, listMapStringListStringStructure);
		} else {
			Map<String, String> listHashMap = (Map<String, String>) field.getValue();
			record.set(metadata, new MapStringStringStructure(listHashMap));
		}

	}

	private void manageMapStringListStringStructureFactory(Record record, Metadata metadata,
														   Entry<String, Object> field) {

		if (metadata.isMultivalue()) {
			List<Map<String, String>> listMapStringList = (List<Map<String, String>>) field.getValue();
			List<MapStringListStringStructure> mapStringListStringStructureList = new ArrayList<>();

			MapStringListStringStructure currentMapStringListStringStructure;

			for (Map<String, String> currentMapListString : listMapStringList) {
				currentMapStringListStringStructure = new MapStringListStringStructure();
				for (String keySet : currentMapListString.keySet()) {
					String string = currentMapListString.get(keySet);
					currentMapStringListStringStructure.put(keySet, asList(string.split(",")));
				}

				mapStringListStringStructureList.add(currentMapStringListStringStructure);
			}
			record.set(metadata, mapStringListStringStructureList);
		} else {
			Map<String, String> hashMapListString = (Map<String, String>) field.getValue();
			hashMapListString.remove("type");

			MapStringListStringStructure mapStringListStringStructure = new MapStringListStringStructure();

			for (String keySet : hashMapListString.keySet()) {
				String string = hashMapListString.get(keySet);
				mapStringListStringStructure.put(keySet, asList(string.split(",")));
			}

			record.set(metadata, mapStringListStringStructure);
		}

	}

	public void validateMask(Metadata metadata, Object convertedValue, DecoratedValidationsErrors decoratedErrors) {
		MaskedMetadataValidator.validate(decoratedErrors, metadata, convertedValue);
	}

	Object convertScalarValue(TypeBatchImportContext typeBatchImportContext, Metadata metadata, Object value,
							  ValidationErrors errors)
			throws PostponedRecordException, SkippedBecauseOfFailedDependency {
		switch (metadata.getType()) {

			case NUMBER:
				return Double.valueOf((String) value);

			case BOOLEAN:
				return value == null ? null : ALL_BOOLEAN_YES.contains(((String) value).toLowerCase());

			case CONTENT:
				return convertContent(typeBatchImportContext, value, errors);

			case REFERENCE:
				return convertReference(typeBatchImportContext, metadata, (String) value);

			case ENUM:
				return EnumWithSmallCodeUtils.toEnum(metadata.getEnumClass(), (String) value);

			default:
				return value;
		}
	}

	private Content convertContent(TypeBatchImportContext typeBatchImportContext, Object value,
								   ValidationErrors errors) {
		ImportContent contentImport = (ImportContent) value;

		Content content;
		if (contentImport == null) {
			content = null;

		} else if (contentImport instanceof SimpleImportContent) {
			content = convertContent(typeBatchImportContext, (SimpleImportContent) value, errors);
			validateHashExists(content, errors, false);

		} else if (contentImport instanceof StructureImportContent) {
			content = convertContent(typeBatchImportContext, (StructureImportContent) value, errors);
			validateHashExists(content, errors, true);

		} else {
			throw new ImpossibleRuntimeException("Unsupported ImportContent : " + contentImport.getClass().getSimpleName());
		}

		return content;
	}

	private void validateHashExists(Content content, ValidationErrors errors, boolean warnings) {

		ContentDao contentDao = modelLayerFactory.getDataLayerFactory().getContentsDao();

		if (content != null) {
			for (String hash : content.getHashOfAllVersions()) {
				if (!contentDao.isDocumentExisting(hash)) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("hash", hash);

					if (contentDao instanceof FileSystemContentDao) {
						parameters.put("filePath", ((FileSystemContentDao) contentDao).getFileOf(hash).getAbsolutePath());
					}

					if (warnings) {
						errors.addWarning(RecordsImportServices.class, HASH_NOT_FOUND_IN_VAULT, parameters);
					} else {
						errors.add(RecordsImportServices.class, HASH_NOT_FOUND_IN_VAULT, parameters);
					}
				}
			}
		}

	}

	private Content convertContent(TypeBatchImportContext typeBatchImportContext, StructureImportContent contentImport,
								   ValidationErrors errors) {
		UserSerializedContentFactory contentFactory = new UserSerializedContentFactory(collection, modelLayerFactory);
		return (Content) contentFactory
				.build(contentImport.getSerializedStructure(), params.isAllowingReferencesToNonExistingUsers());
	}

	private Content convertContent(TypeBatchImportContext typeBatchImportContext, SimpleImportContent contentImport,
								   ValidationErrors errors) {
		List<ContentImportVersion> versions = contentImport.getVersions();
		Content content = null;

		for (int i = 0; i < versions.size(); i++) {
			ContentImportVersion version = versions.get(i);
			try {

				ContentVersionDataSummary contentVersionDataSummary;
				if (version.getUrl().toLowerCase().startsWith("imported://")) {
					String importedFilePath = IMPORTED_FILEPATH_CLEANER.replaceOn(
							version.getUrl().substring("imported://".length()));
					Factory<ContentVersionDataSummary> factory = importedFilesMap.get(importedFilePath);

					if (factory == null) {
						Map<String, Object> parameters = new HashMap<>();
						parameters.put("fileName", contentImport.getFileName());
						parameters.put("filePath", importedFilePath);
						errors.add(RecordsImportServices.class, CONTENT_NOT_IMPORTED_ERROR, parameters);
						return null;
					} else {
						contentVersionDataSummary = factory.get();
					}

				} else {
					contentVersionDataSummary = typeBatchImportContext.bulkUploader.get(version.getUrl());
				}

				if (content == null) {
					try {

						if (version.isMajor()) {
							content = contentManager.createMajor(user, version.getFileName(), contentVersionDataSummary);
						} else {
							content = contentManager.createMinor(user, version.getFileName(), contentVersionDataSummary);
						}
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> parameters = new HashMap<>();
						parameters.put("fileName", contentImport.getFileName());
						errors.add(RecordsImportServices.class, CONTENT_NOT_IMPORTED_ERROR, parameters);
						return null;
					}
				} else {
					content.updateContentWithName(user, contentVersionDataSummary, version.isMajor(), version.getFileName());
				}
				if (version.getLastModification() != null) {
					content.setVersionModificationDatetime(version.getLastModification());
				}
				if (version.getComment() != null) {
					content.setVersionComment(version.getComment());
				}
			} catch (BulkUploaderRuntimeException e) {
				e.getCause().printStackTrace();
				LOGGER.warn("Could not retrieve content with url '" + contentImport.getUrl() + "'");

				Map<String, Object> params = new HashMap<>();
				params.put("url", e.getKey());
				errors.addWarning(RecordsImportServices.class, CONTENT_NOT_FOUND_ERROR, params);
				return null;
			}
		}
		return content;
	}

	private Object convertReference(TypeBatchImportContext typeBatchImportContext, Metadata metadata, String value)
			throws PostponedRecordException, SkippedBecauseOfFailedDependency {

		Resolver resolver = toResolver(value);
		String referenceType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();

		if (LEGACY_ID_LOCAL_CODE.equals(resolver.metadata) && resolver.value != null) {
			if (this.skippedRecordsImport.isSkipped(referenceType, resolver.value)) {
				throw new SkippedBecauseOfFailedDependency();
			}
		}

		if (!resolverCache.isAvailable(referenceType, resolver.metadata, resolver.value)) {
			throw new PostponedRecordException();
		}

		if (!typeBatchImportContext.options.isImportAsLegacyId() && value != null && value.startsWith("id:")) {
			return value.substring(3);
		} else {
			return resolverCache.resolve(referenceType, value);
		}
	}

	Object convertValue(TypeBatchImportContext typeBatchImportContext, Metadata metadata, Object value,
						ValidationErrors errors)
			throws PostponedRecordException, SkippedBecauseOfFailedDependency {

		if (metadata.isMultivalue()) {

			List<Object> convertedValues = new ArrayList<>();

			if (value != null) {
				List<Object> rawValues = (List<Object>) value;
				for (Object item : rawValues) {
					Object convertedValue = convertScalarValue(typeBatchImportContext, metadata, item, errors);
					if (convertedValue != null) {
						convertedValues.add(convertedValue);
					}
				}
			}

			return convertedValues;

		} else {
			return convertScalarValue(typeBatchImportContext, metadata, value, errors);
		}

	}

	List<String> getImportedSchemaTypes()
			throws ValidationException {
		List<String> importedSchemaTypes = new ArrayList<>();
		List<String> availableSchemaTypes = importDataProvider.getAvailableSchemaTypes();

		validateAvailableSchemaTypes(availableSchemaTypes);

		for (String schemaType : types.getSchemaTypesSortedByDependency()) {
			if (availableSchemaTypes.contains(schemaType)) {
				importedSchemaTypes.add(schemaType);
			}
		}

		if (importedSchemaTypes.contains(Event.SCHEMA_TYPE)) {
			importedSchemaTypes.remove(Event.SCHEMA_TYPE);
			importedSchemaTypes.add(Event.SCHEMA_TYPE);
		}

		return importedSchemaTypes;
	}

	private void validateAvailableSchemaTypes(List<String> availableSchemaTypes)
			throws ValidationException {
		ValidationErrors errors = new ValidationErrors();

		for (String availableSchemaType : availableSchemaTypes) {
			try {
				types.getSchemaType(availableSchemaType);
			} catch (NoSuchSchemaType e) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("schemaType", availableSchemaType);

				errors.add(RecordsImportServices.class, INVALID_SCHEMA_TYPE_CODE, parameters);
			}
		}

		if (!errors.getValidationErrors().isEmpty()) {
			throw new ValidationException(errors);
		}
	}

	void validate(ValidationErrors errors)
			throws com.constellio.model.frameworks.validation.ValidationException {
		List<String> importedSchemaTypes = getImportedSchemaTypes();
		for (String schemaType : importedSchemaTypes) {
			new RecordsImportValidator(schemaType, progressionHandler, importDataProvider, types, resolverCache, extensions,
					language, skippedRecordsImport, params).validate(errors);

			if (params.getImportValidationErrorsBehavior() == ImportValidationErrorsBehavior.STOP_IMPORT) {
				errors.throwIfNonEmpty();
			}
		}
	}

	MetadataSchema getMetadataSchema(String schema) {
		return schemasManager.getSchemaTypes(collection).getSchema(schema);
	}

	MetadataSchemaType getMetadataSchemaType(String schemaType) {
		return schemasManager.getSchemaTypes(collection).getSchemaType(schemaType);
	}

	private static class PostponedRecordException extends Exception {
	}

	private static class SkippedBecauseOfFailedDependency extends Exception {
	}

}
