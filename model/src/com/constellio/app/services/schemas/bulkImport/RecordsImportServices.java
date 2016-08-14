package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.app.services.schemas.bulkImport.Resolver.toResolver;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.schemas.bulkImport.RecordsImportServicesRuntimeException.RecordsImportServicesRuntimeException_CyclicDependency;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.frameworks.validation.DecoratedValidationsErrors;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.BulkUploader;
import com.constellio.model.services.contents.BulkUploaderRuntimeException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.ContentImport;
import com.constellio.model.services.records.ContentImportVersion;
import com.constellio.model.services.records.RecordCachesServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.bulkImport.ProgressionHandler;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class RecordsImportServices implements ImportServices {
	public static final String INVALID_SCHEMA_TYPE_CODE = "invalidSchemaTypeCode";
	public static final String LEGACY_ID_LOCAL_CODE = LEGACY_ID.getLocalCode();

	static final List<String> ALL_BOOLEAN_YES = asList("o", "y", "t", "oui", "vrai", "yes", "true", "1");
	static final List<String> ALL_BOOLEAN_NO = asList("n", "f", "non", "faux", "no", "false", "0");

	private static final String IMPORT_URL_INPUTSTREAM_NAME = "RecordsImportServices-ImportURL";

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsImportServices.class);

	private static final int DEFAULT_BATCH_SIZE = 100;

	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemasManager schemasManager;
	private RecordServices recordServices;
	private ContentManager contentManager;
	private IOServices ioServices;
	private SearchServices searchServices;
	private int batchSize;
	private URLResolver urlResolver;

	public RecordsImportServices(ModelLayerFactory modelLayerFactory, int batchSize) {
		this(modelLayerFactory, batchSize, new DefaultURLResolver(modelLayerFactory.getIOServicesFactory().newIOServices()));
	}

	public RecordsImportServices(ModelLayerFactory modelLayerFactory, int batchSize, URLResolver urlResolver) {
		this.modelLayerFactory = modelLayerFactory;
		this.batchSize = batchSize;
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		recordServices = modelLayerFactory.newRecordServices();
		searchServices = modelLayerFactory.newSearchServices();
		contentManager = modelLayerFactory.getContentManager();
		ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		this.urlResolver = urlResolver;
	}

	public RecordsImportServices(ModelLayerFactory modelLayerFactory) {
		this(modelLayerFactory, DEFAULT_BATCH_SIZE);
	}

	public RecordsImportServices(ModelLayerFactory modelLayerFactory, URLResolver urlResolver) {
		this(modelLayerFactory, DEFAULT_BATCH_SIZE, urlResolver);
	}

	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener, final User user)
			throws RecordsImportServicesRuntimeException, ValidationException {
		return bulkImport(importDataProvider, bulkImportProgressionListener, user, new ArrayList<String>());
	}

	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener, final User user,
			final BulkImportParams params)
			throws RecordsImportServicesRuntimeException, ValidationException {
		return bulkImport(importDataProvider, bulkImportProgressionListener, user, new ArrayList<String>(), params);
	}

	@Override
	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener,
			final User user, List<String> collections)
			throws RecordsImportServicesRuntimeException, ValidationException {
		return bulkImport(importDataProvider, bulkImportProgressionListener, user, collections, new BulkImportParams());
	}

	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener,
			final User user, List<String> collections, BulkImportParams params)
			throws RecordsImportServicesRuntimeException, ValidationException {

		Language language = Language.withCode(user.getLoginLanguageCode());
		if (language == null) {
			language = Language.withCode(modelLayerFactory.getConfiguration().getMainDataLanguage());
		}

		ProgressionHandler progressionHandler = new ProgressionHandler(bulkImportProgressionListener);
		String collection = user.getCollection();
		new RecordCachesServices(modelLayerFactory).loadCachesIn(collection);
		importDataProvider.initialize();
		try {
			ModelLayerCollectionExtensions extensions = modelLayerFactory.getExtensions().forCollection(collection);
			MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);

			ResolverCache resolverCache = new ResolverCache(modelLayerFactory.newRecordServices(),
					modelLayerFactory.newSearchServices(), types, importDataProvider);
			validate(importDataProvider, progressionHandler, user, types, resolverCache, extensions, language);
			return run(importDataProvider, progressionHandler, user, types, resolverCache, extensions, params, language);
		} finally {
			importDataProvider.close();
		}
	}

	void validate(ImportDataProvider importDataProvider, ProgressionHandler progressionHandler, User user,
			MetadataSchemaTypes types, ResolverCache resolverCache, ModelLayerCollectionExtensions extensions,
			Language language)
			throws com.constellio.model.frameworks.validation.ValidationException {
		List<String> importedSchemaTypes = getImportedSchemaTypes(types, importDataProvider);

		for (String schemaType : importedSchemaTypes) {
			new RecordsImportValidator(schemaType, progressionHandler, importDataProvider, types, resolverCache, extensions,
					language).validate();
		}
	}

	private BulkImportResults run(ImportDataProvider importDataProvider, ProgressionHandler progressionHandler,
			User user, MetadataSchemaTypes types, ResolverCache resolverCache, ModelLayerCollectionExtensions extensions,
			BulkImportParams params, Language language)
			throws ValidationException {

		BulkImportResults importResults = new BulkImportResults();
		List<String> importedSchemaTypes = getImportedSchemaTypes(types, importDataProvider);
		for (String schemaType : importedSchemaTypes) {

			boolean recordsBeforeImport = searchServices.hasResults(from(types.getSchemaType(schemaType)).returnAll());
			progressionHandler.beforeImportOf(schemaType);
			List<String> uniqueMetadatas = types.getSchemaType(schemaType).getAllMetadatas()
					.onlyWithType(STRING).onlyUniques().toLocalCodesList();
			int previouslySkipped = 0;
			AtomicInteger addUpdateCount = new AtomicInteger();
			boolean typeImportFinished = false;
			while (!typeImportFinished) {

				ImportDataIterator importDataIterator = importDataProvider.newDataIterator(schemaType);
				int skipped = bulkImport(importResults, uniqueMetadatas, resolverCache, importDataIterator, schemaType,
						progressionHandler, user, types, extensions, addUpdateCount, params, recordsBeforeImport, language);
				if (skipped > 0 && skipped == previouslySkipped) {
					Set<String> cyclicDependentIds = resolverCache.getNotYetImportedLegacyIds(schemaType);
					throw new RecordsImportServicesRuntimeException_CyclicDependency(schemaType,
							new ArrayList<>(cyclicDependentIds));
				}
				if (skipped == 0) {
					typeImportFinished = true;
				}
				previouslySkipped = skipped;
			}
		}
		progressionHandler.onImportFinished();
		return importResults;
	}

	int bulkImport(BulkImportResults importResults, List<String> uniqueMetadatas, ResolverCache resolverCache,
			ImportDataIterator importDataIterator, String schemaType, ProgressionHandler progressionHandler, User user,
			MetadataSchemaTypes types, ModelLayerCollectionExtensions extensions, AtomicInteger addUpdateCount,
			BulkImportParams params, boolean recordsBeforeImport, Language language)
			throws ValidationException {

		int skipped = 0;
		Iterator<List<ImportData>> importDataBatches = new BatchBuilderIterator<>(importDataIterator, batchSize);
		ValidationErrors errors = new ValidationErrors();
		while (importDataBatches.hasNext()) {
			try {
				List<ImportData> batch = importDataBatches.next();
				Transaction transaction = new Transaction();
				transaction.getRecordUpdateOptions().setSkipReferenceValidation(true);
				if (!recordsBeforeImport) {
					transaction.getRecordUpdateOptions().setUnicityValidationsEnabled(false);
				}
				skipped += importBatch(importResults, uniqueMetadatas, resolverCache, schemaType, user, batch, transaction,
						types, progressionHandler, extensions, addUpdateCount, errors, params, importDataIterator.getOptions(),
						language);
				recordServices.executeHandlingImpactsAsync(transaction);
			} catch (RecordServicesException e) {
				while (importDataBatches.hasNext()) {
					importDataBatches.next();
				}
				throw new RuntimeException(e);
			}

		}

		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}

		return skipped;
	}

	private int importBatch(BulkImportResults importResults, List<String> uniqueMetadatas, ResolverCache resolverCache,
			final String schemaType, User user, List<ImportData> batch, Transaction transaction, MetadataSchemaTypes types,
			ProgressionHandler progressionHandler, ModelLayerCollectionExtensions extensions, AtomicInteger addUpdateCount,
			ValidationErrors errors, BulkImportParams params, ImportDataOptions options, Language language)
			throws ValidationException {

		BulkUploader bulkUploader = null;
		List<Metadata> contentMetadatas = types.getAllContentMetadatas();
		if (!contentMetadatas.isEmpty()) {
			bulkUploader = new BulkUploader(modelLayerFactory);
			bulkUploader.setHandleDeletionOfUnreferencedHashes(false);
			for (ImportData toImport : batch) {

				for (Metadata contentMetadata : contentMetadatas) {
					if (toImport.getFields().containsKey(contentMetadata.getLocalCode())) {
						List<ContentImport> contentImports = new ArrayList<>();
						if (contentMetadata.isMultivalue()) {
							contentImports.addAll((List) toImport.getFields().get(contentMetadata.getLocalCode()));
						} else {
							contentImports.add((ContentImport) toImport.getFields().get(contentMetadata.getLocalCode()));

						}
						for (ContentImport contentImport : contentImports) {
							for (Iterator<ContentImportVersion> iterator = contentImport.getVersions().iterator(); iterator
									.hasNext(); ) {
								ContentImportVersion version = iterator.next();
								String url = version.getUrl();
								StreamFactory<InputStream> inputStreamStreamFactory = urlResolver
										.resolve(url, version.getFileName());
								if (iterator.hasNext()) {
									bulkUploader.uploadAsyncWithoutParsing(url, inputStreamStreamFactory, version.getFileName());
								} else {
									bulkUploader.uploadAsync(url, inputStreamStreamFactory);
								}
							}

						}
					}
				}

			}
			bulkUploader.close();
		}

		int skipped = 0;

		for (final ImportData toImport : batch) {
			final String schemaTypeLabel = types.getSchemaType(schemaType).getLabel(language);

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
					parameters.put("schemaType", schemaType);
				}
			};

			extensions.callRecordImportValidate(schemaType, new ValidationParams(decoratedValidationsErrors, toImport));

			if (!decoratedValidationsErrors.getValidationErrors().isEmpty() && params.isStopOnFirstError()) {
				throw new ValidationException(decoratedValidationsErrors);
			}

			String legacyId = toImport.getLegacyId();
			if (resolverCache.getNotYetImportedLegacyIds(schemaType).contains(legacyId)) {

				Object title = toImport.getFields().get("title");
				progressionHandler.onRecordImport(addUpdateCount.get(), legacyId, (String) title);

				try {

					Record record = buildRecord(bulkUploader, importResults, user, resolverCache, user.getCollection(),
							schemaType, toImport, types, extensions, options);
					transaction.add(record);
					addUpdateCount.incrementAndGet();

					try {
						recordServices.validateRecordInTransaction(record, transaction);

					} catch (RecordServicesException.ValidationException e) {
						for (ValidationError error : e.getErrors().getValidationErrors()) {
							decoratedValidationsErrors.add(error, error.getParameters());
						}

						if (params.isStopOnFirstError()) {
							throw new ValidationException(decoratedValidationsErrors);
						} else {
							transaction.remove(record);
						}
					}

					progressionHandler.incrementProgression();

					resolverCache.mapIds(schemaType, LEGACY_ID_LOCAL_CODE, legacyId, record.getId());
					for (String uniqueMetadata : uniqueMetadatas) {
						String value = (String) toImport.getFields().get(uniqueMetadata);
						if (value != null) {
							resolverCache.mapIds(schemaType, uniqueMetadata, value, record.getId());
						}
					}
				} catch (SkippedRecordException e) {
					progressionHandler.onRecordImportPostponed(legacyId);
					skipped++;
				}
			}
		}

		contentManager.deleteUnreferencedContents(RecordsFlushing.LATER());
		return skipped;
	}

	Record buildRecord(BulkUploader bulkUploader, BulkImportResults importResults, User user, ResolverCache resolverCache,
			String collection, String schemaTypeCode, ImportData toImport, MetadataSchemaTypes types,
			ModelLayerCollectionExtensions extensions, ImportDataOptions options)
			throws SkippedRecordException {
		MetadataSchemaType schemaType = getMetadataSchemaType(collection, schemaTypeCode);
		MetadataSchema newSchema = getMetadataSchema(collection, schemaTypeCode + "_" + toImport.getSchema());

		Record record;
		String legacyId = toImport.getLegacyId();
		if (resolverCache.isRecordUpdate(schemaTypeCode, legacyId)) {
			record = modelLayerFactory.newSearchServices()
					.searchSingleResult(from(schemaType).where(LEGACY_ID).isEqualTo(legacyId));
		} else {
			if (options.isImportAsLegacyId()) {
				record = recordServices.newRecordWithSchema(newSchema);
			} else {
				record = recordServices.newRecordWithSchema(newSchema, legacyId);
			}
		}

		if (!newSchema.getCode().equals(record.getSchemaCode())) {
			MetadataSchema wasSchema = getMetadataSchema(collection, record.getSchemaCode());
			record.changeSchema(wasSchema, newSchema);
		}
		record.set(LEGACY_ID, legacyId);
		for (Entry<String, Object> field : toImport.getFields().entrySet()) {
			Metadata metadata = newSchema.getMetadata(field.getKey());
			if (metadata.getType() != MetadataValueType.STRUCTURE) {
				Object value = field.getValue();
				Object convertedValue =
						value == null ? null : convertValue(bulkUploader, importResults, user, resolverCache, metadata, value);
				record.set(metadata, convertedValue);
			}
		}

		extensions.callRecordImportBuild(schemaTypeCode, new BuildParams(record, types, toImport));

		return record;
	}

	Object convertScalarValue(BulkUploader bulkUploader, BulkImportResults importResults, User user, ResolverCache resolverCache,
			Metadata metadata,
			Object value)
			throws SkippedRecordException {
		switch (metadata.getType()) {

		case NUMBER:
			return Double.valueOf((String) value);

		case BOOLEAN:
			return value == null ? null : ALL_BOOLEAN_YES.contains(((String) value).toLowerCase());

		case CONTENT:
			return convertContent(bulkUploader, importResults, user, value);

		case REFERENCE:
			return convertReference(resolverCache, metadata, (String) value);

		case ENUM:
			return EnumWithSmallCodeUtils.toEnum(metadata.getEnumClass(), (String) value);

		default:
			return value;
		}
	}

	private Content convertContent(BulkUploader uploader, BulkImportResults importResults, User user, Object value) {
		ContentImport contentImport = (ContentImport) value;
		List<ContentImportVersion> versions = contentImport.getVersions();
		Content content = null;

		for (int i = 0; i < versions.size(); i++) {
			ContentImportVersion version = versions.get(i);
			try {
				ContentVersionDataSummary contentVersionDataSummary = uploader.get(version.getUrl());
				if (content == null) {
					if (version.isMajor()) {
						content = contentManager.createMajor(user, version.getFileName(), contentVersionDataSummary);
					} else {
						content = contentManager.createMinor(user, version.getFileName(), contentVersionDataSummary);
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
				importResults.add(new ImportError(contentImport.getUrl(), ""));
				return null;
			}
		}
		return content;
	}

	private Object convertReference(ResolverCache resolverCache, Metadata metadata, String value)
			throws SkippedRecordException {

		Resolver resolver = toResolver(value);

		String referenceType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
		if (!resolverCache.isAvailable(referenceType, resolver.metadata, resolver.value)) {
			throw new SkippedRecordException();
		}
		return resolverCache.resolve(referenceType, value);
	}

	Object convertValue(BulkUploader bulkUploader, BulkImportResults importResults, User user, ResolverCache resolverCache,
			Metadata metadata, Object value)
			throws SkippedRecordException {

		if (metadata.isMultivalue()) {

			List<Object> convertedValues = new ArrayList<>();

			if (value != null) {
				List<Object> rawValues = (List<Object>) value;
				for (Object item : rawValues) {
					Object convertedValue = convertScalarValue(bulkUploader, importResults, user, resolverCache, metadata, item);
					if (convertedValue != null) {
						convertedValues.add(convertedValue);
					}
				}
			}

			return convertedValues;

		} else {
			return convertScalarValue(bulkUploader, importResults, user, resolverCache, metadata, value);
		}

	}

	List<String> getImportedSchemaTypes(MetadataSchemaTypes types, ImportDataProvider importDataProvider)
			throws ValidationException {
		List<String> importedSchemaTypes = new ArrayList<>();
		List<String> availableSchemaTypes = importDataProvider.getAvailableSchemaTypes();

		validateAvailableSchemaTypes(types, availableSchemaTypes);

		for (String schemaType : types.getSchemaTypesSortedByDependency()) {
			if (availableSchemaTypes.contains(schemaType)) {
				importedSchemaTypes.add(schemaType);
			}
		}

		return importedSchemaTypes;
	}

	private void validateAvailableSchemaTypes(MetadataSchemaTypes types, List<String> availableSchemaTypes)
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

	MetadataSchema getMetadataSchema(String collection, String schema) {
		return schemasManager.getSchemaTypes(collection).getSchema(schema);
	}

	MetadataSchemaType getMetadataSchemaType(String collection, String schemaType) {
		return schemasManager.getSchemaTypes(collection).getSchemaType(schemaType);
	}

	private static class SkippedRecordException extends Exception {
	}

}
