package com.constellio.model.services.contents;

import static com.constellio.model.entities.enums.ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.io.ConversionManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactory;
import com.constellio.data.io.streamFactories.services.one.StreamOperationThrowingException;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.contents.ContentManagerException.ContentManagerException_ContentNotParsed;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_CannotReadInputStream;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_CannotReadParsedContent;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_CannotSaveContent;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.contents.icap.IcapService;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ContentManager implements StatefulService {

	//TODO Increase this limit to 100
	private static final int REPARSE_REINDEX_BATCH_SIZE = 1;

	public static final String READ_CONTENT_FOR_PREVIEW_CONVERSION = "ContentManager-ReadContentForPreviewConversion";

	static final String CONTENT_IMPORT_THREAD = "ContentImportThread";

	static final String BACKGROUND_THREAD = "DeleteUnreferencedContent";

	static final String READ_PARSED_CONTENT = "ContentServices-ReadParsedContent";

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentManager.class);
	private final RecordDao recordDao;
	FileParser fileParser;
	private final HashingService hashingService;
	private final IOServices ioServices;
	private final UniqueIdGenerator uniqueIdGenerator;
	private final SearchServices searchServices;
	private final BackgroundThreadsManager backgroundThreadsManager;
	private final MetadataSchemasManager metadataSchemasManager;
	private final ModelLayerConfiguration configuration;
	private final RecordServices recordServices;
	private final CollectionsListManager collectionsListManager;
	private final AtomicBoolean closing = new AtomicBoolean();
	private final ModelLayerFactory modelLayerFactory;
	private final IcapService icapService;

	private boolean serviceThreadEnabled = true;

	public ContentManager(ModelLayerFactory modelLayerFactory) {
		this(modelLayerFactory, new IcapService(modelLayerFactory));
	}

	public ContentManager(ModelLayerFactory modelLayerFactory, IcapService icapService) {
		super();
		this.modelLayerFactory = modelLayerFactory;
		this.recordDao = modelLayerFactory.getDataLayerFactory().newRecordDao();
		this.fileParser = modelLayerFactory.newFileParser();
		this.hashingService = modelLayerFactory.getDataLayerFactory().getIOServicesFactory()
				.newHashingService(modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration().getHashingEncoding());
		this.ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		this.uniqueIdGenerator = modelLayerFactory.getDataLayerFactory().getUniqueIdGenerator();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.backgroundThreadsManager = modelLayerFactory.getDataLayerFactory().getBackgroundThreadsManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.configuration = modelLayerFactory.getConfiguration();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.icapService = icapService;
	}

	@Override
	public void initialize() {
		Runnable contentActionsInBackgroundRunnable = new Runnable() {

			@Override
			public void run() {
				if (serviceThreadEnabled && ReindexingServices.getReindexingInfos() == null) {
					if (modelLayerFactory.getConfiguration().isDeleteUnusedContentEnabled()) {
						deleteUnreferencedContents();
					}
					convertPendingContentForPreview();
					handleRecordsMarkedForParsing();
				}
			}
		};

		backgroundThreadsManager.configure(
				BackgroundThreadConfiguration.repeatingAction(BACKGROUND_THREAD, contentActionsInBackgroundRunnable)
						.executedEvery(
								configuration.getUnreferencedContentsThreadDelayBetweenChecks())
						.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE));

		if (configuration.getContentImportThreadFolder() != null) {
			Runnable contentImportAction = new Runnable() {

				@Override
				public void run() {
					uploadFilesInImportFolder();
				}
			};
			backgroundThreadsManager.configure(
					BackgroundThreadConfiguration.repeatingAction(CONTENT_IMPORT_THREAD, contentImportAction)
							.executedEvery(Duration.standardSeconds(30))
							.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE));
		}

		//
		icapService.init();
	}

	public ContentManager setServiceThreadEnabled(boolean serviceThreadEnabled) {
		this.serviceThreadEnabled = serviceThreadEnabled;
		return this;
	}

	@Override
	public void close() {
		closing.set(true);
	}

	public Content createWithVersion(User user, String filename, ContentVersionDataSummary newVersion, String version) {
		String uniqueId = uniqueIdGenerator.next();
		return ContentImpl.createWithVersion(uniqueId, user, filename, newVersion, version, false);
	}

	public Content createMajor(User user, String filename, ContentVersionDataSummary newVersion) {
		String uniqueId = uniqueIdGenerator.next();
		return ContentImpl.create(uniqueId, user, filename, newVersion, true, false);
	}

	public Content createMinor(User user, String filename, ContentVersionDataSummary newVersion) {
		String uniqueId = uniqueIdGenerator.next();
		return ContentImpl.create(uniqueId, user, filename, newVersion, false, false);
	}

	public Content createEmptyMinor(User user, String filename, ContentVersionDataSummary newVersion) {
		String uniqueId = uniqueIdGenerator.next();
		return ContentImpl.create(uniqueId, user, filename, newVersion, false, true);
	}

	public Content createFileSystem(String filename, ContentVersionDataSummary version) {
		return ContentImpl.createSystemContent(filename, version);
	}

	public boolean hasContentPreview(String hash) {
		return getContentDao().isDocumentExisting(hash + ".preview");
	}

	public ContentDao getContentDao() {
		return modelLayerFactory.getDataLayerFactory().getContentsDao();
	}

	public InputStream getContentPreviewInputStream(String hash, String streamName) {
		try {
			return getContentDao().getContentInputStream(hash + ".preview", streamName);
		} catch (ContentDaoException_NoSuchContent e) {
			throw new ContentManagerRuntimeException.ContentManagerRuntimeException_ContentHasNoPreview(hash);
		}
	}

	public InputStream getContentInputStream(String id, String streamName)
			throws ContentManagerRuntimeException_NoSuchContent {
		try {
			return getContentDao().getContentInputStream(id, streamName);
		} catch (ContentDaoException.ContentDaoException_NoSuchContent e) {
			throw new ContentManagerRuntimeException_NoSuchContent(id, e);
		}
	}

	public CloseableStreamFactory<InputStream> getContentInputStreamFactory(String id)
			throws ContentManagerRuntimeException_NoSuchContent {

		try {
			return getContentDao().getContentInputStreamFactory(id);
		} catch (ContentDaoException.ContentDaoException_NoSuchContent e) {
			throw new ContentManagerRuntimeException_NoSuchContent(id, e);
		}
	}

	void saveContent(String id, CopyInputStreamFactory streamFactory)
			throws ContentManagerRuntimeException_CannotSaveContent {

		try {
			getContentDao().moveFileToVault(streamFactory.getTempFile(), id);
		} catch (ContentDaoRuntimeException e) {
			throw new ContentManagerRuntimeException_CannotSaveContent(e);
		}

	}

	void saveParsedContent(String id, ParsedContent parsingResults)
			throws ContentManagerRuntimeException_CannotSaveContent {

		String parsingResultsString = writeParsingResults(parsingResults);

		StreamFactory<InputStream> parsingResultsStreamFactory = ioServices.newInputStreamFactory(parsingResultsString);
		try {
			ioServices.execute(addParsecContentInContentDaoOperation(id), parsingResultsStreamFactory);
		} catch (ContentDaoException | IOException e) {
			throw new ContentManagerRuntimeException_CannotSaveContent(e);
		}

	}

	String writeParsingResults(ParsedContent parsingResults) {
		return new ParsedContentConverter().convertToString(parsingResults);
	}

	@Deprecated
	public ContentVersionDataSummary upload(InputStream inputStream) {
		return upload(inputStream, new UploadOptions()).getContentVersionDataSummary();
	}

	public ContentVersionDataSummaryResponse upload(InputStream inputStream, String filename) {
		return upload(inputStream, new UploadOptions(filename));
	}

	@Deprecated
	public ContentVersionDataSummaryResponse upload(InputStream inputStream, boolean handleDeletionOfUnreferencedHashes,
			boolean parse,
			String fileName) {
		return upload(inputStream, new UploadOptions(handleDeletionOfUnreferencedHashes, parse, false, fileName));
	}

	public ContentVersionDataSummaryResponse upload(InputStream inputStream, UploadOptions uploadOptions) {
		String fileName = uploadOptions.getFileName();
		boolean handleDeletionOfUnreferencedHashes = uploadOptions.isHandleDeletionOfUnreferencedHashes();

		ConstellioEIMConfigs configs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		boolean defaultParsing = configs.getDefaultParsingBehavior() == SYNC_PARSING_FOR_ALL_CONTENTS;

		boolean parse = uploadOptions.isParse(defaultParsing);

		CopyInputStreamFactory closeableInputStreamFactory = ioServices.copyToReusableStreamFactory(
				inputStream, fileName);

		try {
			String hash = hashingService.getHashFromStream(closeableInputStreamFactory);
			try (final InputStream icapInputStream = closeableInputStreamFactory.create(hash + ".icapscan")) {
				if (closeableInputStreamFactory instanceof CopyInputStreamFactory) {
					icapService.scan(fileName, icapInputStream);
				}
			}

			if (handleDeletionOfUnreferencedHashes) {
				markForDeletionIfNotReferenced(hash);
			}
			String mimeType;
			boolean duplicate = false;
			if (parse) {
				ParsedContentResponse parsedContentResponse = getPreviouslyParsedContentOrParseFromStream(hash,
						closeableInputStreamFactory);
				ParsedContent parsedContent = parsedContentResponse.getParsedContent();
				mimeType = parsedContent.getMimeType();
				if (mimeType == null) {
					mimeType = detectMimetype(closeableInputStreamFactory, fileName);
				}
				duplicate = parsedContentResponse.hasFoundDuplicate();
			} else {
				mimeType = detectMimetype(closeableInputStreamFactory, fileName);
			}
			//saveContent(hash, closeableInputStreamFactory);
			long length = closeableInputStreamFactory.length();
			saveContent(hash, closeableInputStreamFactory);
			return new ContentVersionDataSummaryResponse(duplicate, new ContentVersionDataSummary(hash, mimeType, length));

		} catch (HashingServiceException | IOException e) {
			throw new ContentManagerRuntimeException_CannotReadInputStream(e);

		} finally {
			ioServices.closeQuietly(closeableInputStreamFactory);
		}
	}

	int contentVersionSummary = 0;

	public ContentVersionDataSummaryResponse getContentVersionSummary(String hash) {
		ParsedContentResponse parsedContentResponse = getParsedContentParsingIfNotYetDone(hash);
		ParsedContent parsedContent = (ParsedContent) parsedContentResponse.getParsedContent();
		return new ContentVersionDataSummaryResponse(parsedContentResponse.hasFoundDuplicate(),
				new ContentVersionDataSummary(hash, parsedContent.getMimeType(), parsedContent.getLength()));
	}

	public boolean isParsed(String hash) {
		try {
			getParsedContent(hash);
			return true;
		} catch (ContentManagerException_ContentNotParsed e) {
			return false;
		}
	}

	ParsedContentResponse getPreviouslyParsedContentOrParseFromStream(String hash,
			CloseableStreamFactory<InputStream> inputStreamFactory)
			throws IOException {

		ParsedContent parsedContent;
		ParsedContentResponse response;
		try {
			parsedContent = getParsedContent(hash);
			response = new ParsedContentResponse(true, parsedContent);
		} catch (ContentManagerException_ContentNotParsed e) {
			parsedContent = parseAndSave(hash, inputStreamFactory);
			response = new ParsedContentResponse(false, parsedContent);
		}
		return response;
	}

	private ParsedContent parseAndSave(String hash, CloseableStreamFactory<InputStream> inputStreamFactory)
			throws IOException {
		ParsedContent parsedContent = tryToParse(inputStreamFactory);
		saveParsedContent(hash, parsedContent);
		return parsedContent;
	}

	private String detectMimetype(CloseableStreamFactory<InputStream> inputStreamFactory, String fileName) {
		try {
			return fileParser.detectMimetype(inputStreamFactory, fileName);
		} catch (FileParserException e) {
			return e.getDetectedMimetype();
		}
	}

	ParsedContent tryToParse(CloseableStreamFactory<InputStream> inputStreamFactory)
			throws IOException {
		try {
			return fileParser.parse(inputStreamFactory, inputStreamFactory.length());

		} catch (FileParserException e) {
			return ParsedContent.unparsable(e.getDetectedMimetype(), inputStreamFactory.length());

		}
	}

	public StreamOperationThrowingException<InputStream, ContentDaoException> addParsecContentInContentDaoOperation(
			final String newContentId) {
		return new StreamOperationThrowingException<InputStream, ContentDaoException>() {

			@Override
			public void execute(InputStream stream)
					throws ContentDaoException {
				getContentDao().add(newContentId + "__parsed", stream);
			}
		};
	}

	public StreamOperationThrowingException<InputStream, ContentDaoException> addInContentDaoOperation(
			final String newContentId) {
		return new StreamOperationThrowingException<InputStream, ContentDaoException>() {

			@Override
			public void execute(InputStream stream)
					throws ContentDaoException {
				getContentDao().add(newContentId, stream);
			}
		};
	}

	public void silentlyMarkForDeletionIfNotReferenced(String hash) {
		try {
			markForDeletionIfNotReferenced(hash);
		} catch (ContentManagerRuntimeException e) {
			LOGGER.warn("Cannot mark hash for potential deletion'" + hash + "'. ");
		}

	}

	public void markForDeletionIfNotReferenced(String hash) {

		String id = UUIDV1Generator.newRandomId();
		Map<String, Object> fields = new HashMap<>();
		fields.put("type_s", "marker");
		fields.put("contentMarkerHash_s", hash);
		fields.put("time_dt", TimeProvider.getLocalDateTime());

		RecordDTO recordDTO = new RecordDTO(id, fields);
		try {
			recordDao.execute(new TransactionDTO(RecordsFlushing.LATER()).withNewRecords(asList(recordDTO)));
		} catch (OptimisticLocking e) {
			throw new ImpossibleRuntimeException(e);
		}
	}

	boolean isReferenced(String hash) {
		boolean referenced = false;
		for (MetadataSchemaTypes types : metadataSchemasManager.getAllCollectionsSchemaTypes()) {
			List<Metadata> contentMetadatas = types.getAllContentMetadatas();
			if (!contentMetadatas.isEmpty()) {
				referenced |= searchServices.hasResults(
						fromAllSchemasIn(types.getCollection()).whereAny(contentMetadatas).is(ContentFactory.isHash(hash)));
			}
		}

		return referenced;
	}

	public void uploadFilesInImportFolder() {
		if (modelLayerFactory.getConfiguration().getContentImportThreadFolder() != null) {
			new ContentManagerImportThreadServices(modelLayerFactory).importFiles();
		}
	}

	public Map<String, Factory<ContentVersionDataSummary>> getImportedFilesMap() {
		return new ContentManagerImportThreadServices(modelLayerFactory).readFileNameSHA1Index();
	}

	public void convertPendingContentForPreview() {

		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			if (!closing.get()) {
				List<Record> records = searchServices.search(new LogicalSearchQuery()
						.setCondition(fromAllSchemasIn(collection).where(Schemas.MARKED_FOR_PREVIEW_CONVERSION).isTrue())
						.setNumberOfRows(20));

				if (!records.isEmpty()) {

					File tempFolder = ioServices.newTemporaryFolder("previewConversion");
					ConversionManager conversionManager = null;
					try {
						conversionManager = new ConversionManager(ioServices, 1, tempFolder);

						Transaction transaction = new Transaction();
						for (Record record : records) {
							if (!closing.get()) {
								convertRecordContents(record, conversionManager);
								transaction.add(record.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, null));
							}
						}
						try {
							recordServices.execute(transaction);
						} catch (RecordServicesException e) {
							throw new RuntimeException(e);
						}

					} finally {
						ioServices.deleteQuietly(tempFolder);
						if (conversionManager != null) {
							conversionManager.close();
						}
					}
				}
			}
		}
	}

	private void convertRecordContents(Record record, ConversionManager conversionManager) {
		MetadataSchema schema = metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
		for (Metadata contentMetadata : schema.getMetadatas().onlyWithType(MetadataValueType.CONTENT)) {
			if (!contentMetadata.isMultivalue()) {
				Content content = record.get(contentMetadata);
				if (content != null) {
					convertContentForPreview(content, conversionManager);
				}
			}
		}
	}

	private void convertContentForPreview(Content content, ConversionManager conversionManager) {
		String hash = content.getCurrentVersion().getHash();
		String filename = content.getCurrentVersion().getFilename();
		ContentDao contentDao = getContentDao();
		if (!contentDao.isDocumentExisting(hash + ".preview")) {
			InputStream inputStream = null;
			try {
				inputStream = contentDao.getContentInputStream(hash, READ_CONTENT_FOR_PREVIEW_CONVERSION);
				File file = conversionManager.convertToPDF(inputStream, filename);
				contentDao.moveFileToVault(file, hash + ".preview");

			} catch (Throwable t) {
				LOGGER.warn("Cannot convert content '" + filename + "' with hash '" + hash + "'", t);

			} finally {
				ioServices.closeQuietly(inputStream);
			}
		}
	}

	public void handleRecordsMarkedForParsing() {
		handleRecordsMarkedForParsing(RecordsFlushing.NOW());
	}

	public void handleRecordsMarkedForParsing(RecordsFlushing recordsFlushing) {

		Set<String> collections = getCollectionsWithFlag(Schemas.MARKED_FOR_PARSING);
		for (String collection : collections) {
			MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
			LogicalSearchQuery query = new LogicalSearchQuery();
			query.setCondition(fromAllSchemasIn(collection).where(Schemas.MARKED_FOR_PARSING).isTrue());
			query.setNumberOfRows(50);

			List<Record> records = searchServices.search(query);

			Transaction tx = new Transaction();
			tx.addAll(records);
			for (Record record : records) {
				MetadataSchema schema = types.getSchema(record.getSchemaCode());
				for (Metadata metadata : schema.getContentMetadatasForPopulate()) {

					for (Content content : record.<Content>getValues(metadata)) {
						String hash = content.getCurrentVersion().getHash();
						if (!isParsed(hash)) {
							try {
								parseAndSave(hash, getContentInputStreamFactory(hash));
							} catch (IOException e) {
								//TODO
							}
						}

					}

				}
				record.set(Schemas.MARKED_FOR_PARSING, null);

			}
			tx.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
			tx.getRecordUpdateOptions().setFullRewrite(true);
			try {
				recordServices.execute(tx);
			} catch (RecordServicesException e) {
				//TODO
				throw new RuntimeException(e);
			}

		}

	}

	private Set<String> getCollectionsWithFlag(Metadata... flags) {

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(fromEveryTypesOfEveryCollection().whereAny(flags).isTrue());
		query.setNumberOfRows(0);
		query.addFieldFacet("collection_s");

		SPEQueryResponse response = searchServices.query(query);

		Set<String> collections = new HashSet<>();
		collections.addAll(response.getFieldFacetValuesWithResults("collection_s"));
		return collections;
	}

	public void deleteUnreferencedContents() {
		deleteUnreferencedContents(RecordsFlushing.NOW());
	}

	public void deleteUnreferencedContents(RecordsFlushing recordsFlushing) {
		List<RecordDTO> potentiallyDeletableContentMarkers;

		while (!(potentiallyDeletableContentMarkers = getNextPotentiallyUnreferencedContentMarkers()).isEmpty()) {
			List<String> hashToDelete = new ArrayList<>();
			for (RecordDTO marker : potentiallyDeletableContentMarkers) {
				if (closing.get()) {
					return;
				}
				String hash = marker.getFields().get("contentMarkerHash_s").toString();
				if (!isReferenced(hash)) {
					hashToDelete.add(hash);
					hashToDelete.add(hash + "__parsed");
				}
				if (!hashToDelete.isEmpty()) {
					getContentDao().delete(hashToDelete);
				}

			}
			try {
				recordDao.execute(
						new TransactionDTO(recordsFlushing).withDeletedRecords(potentiallyDeletableContentMarkers));
			} catch (OptimisticLocking e) {
				throw new ImpossibleRuntimeException(e);
			}
		}
	}

	ContentModificationsBuilder newContentsModificationBuilder(MetadataSchemaTypes metadataSchemaTypes) {
		return new ContentModificationsBuilder(metadataSchemaTypes);
	}

	public ParsedContentResponse getParsedContentParsingIfNotYetDone(String hash) {
		try {
			return new ParsedContentResponse(true, getParsedContent(hash));
		} catch (ContentManagerException_ContentNotParsed e) {
			CloseableStreamFactory<InputStream> streamFactory = getContentInputStreamFactory(hash);
			try {
				return getPreviouslyParsedContentOrParseFromStream(hash, streamFactory);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	public ParsedContent getParsedContent(String hash)
			throws ContentManagerException_ContentNotParsed {
		String parsedContent = null;

		InputStream inputStream = null;

		try {
			inputStream = getContentDao().getContentInputStream(hash + "__parsed", READ_PARSED_CONTENT);
			parsedContent = ioServices.readStreamToString(inputStream);
		} catch (ContentDaoException.ContentDaoException_NoSuchContent e) {
			throw new ContentManagerException_ContentNotParsed(hash);

		} catch (IOException e) {
			throw new ContentManagerRuntimeException_CannotReadInputStream(e);

		} finally {
			ioServices.closeQuietly(inputStream);
		}
		try {
			return newParsedContentConverter().convertToParsedContent(parsedContent);
		} catch (Exception e) {
			throw new ContentManagerRuntimeException_CannotReadParsedContent(e, hash, parsedContent);
		}

	}

	ParsedContentConverter newParsedContentConverter() {
		return new ParsedContentConverter();
	}

	public List<RecordDTO> getNextPotentiallyUnreferencedContentMarkers() {

		LocalDateTime now = TimeProvider.getLocalDateTime();
		LocalDateTime maxTimeForDelete = now.minus(configuration.getDelayBeforeDeletingUnreferencedContents());

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "time_dt:[* TO " + maxTimeForDelete + "Z]");
		params.set("fq", "contentMarkerHash_s:*");
		params.set("rows", 50);

		return recordDao.searchQuery(params);
	}

	public void reparse(String hash) {
		CloseableStreamFactory<InputStream> streamFactory = getContentInputStreamFactory(hash);
		try {
			parseAndSave(hash, streamFactory);
		} catch (IOException e) {
			throw new ContentManagerRuntimeException_CannotSaveContent(e);
		}

		for (MetadataSchemaTypes types : metadataSchemasManager.getAllCollectionsSchemaTypes()) {

			List<Metadata> contentMetadatas = types.getAllContentMetadatas();
			if (!contentMetadatas.isEmpty()) {
				LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(types.getCollection())
						.whereAny(contentMetadatas).is(ContentFactory.isHash(hash)));

				Iterator<Record> recordsToReindexIterator = searchServices.recordsIterator(query, REPARSE_REINDEX_BATCH_SIZE);
				Iterator<List<Record>> recordsListToReindexIterator = new BatchBuilderIterator<>(recordsToReindexIterator,
						REPARSE_REINDEX_BATCH_SIZE);
				while (recordsListToReindexIterator.hasNext()) {
					List<Record> batch = recordsListToReindexIterator.next();
					for (Record record : batch) {
						MetadataSchema schema = types.getSchema(record.getSchemaCode());
						for (Metadata contentMetadata : contentMetadatas) {
							if (schema.hasMetadataWithCode(contentMetadata.getLocalCode())) {
								record.markAsModified(contentMetadata);
							}
						}
					}
					Transaction transaction = new Transaction(batch);
					transaction.setSkippingRequiredValuesValidation(true);
					try {
						recordServices.execute(transaction);
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}

			}

		}
	}

	public static class UploadOptions {
		private boolean handleDeletionOfUnreferencedHashes;
		private Boolean parse;
		private boolean isThrowingException;
		private String fileName;

		public UploadOptions(boolean handleDeletionOfUnreferencedHashes, boolean parse, boolean isThrowingException,
				String fileName) {
			this.handleDeletionOfUnreferencedHashes = handleDeletionOfUnreferencedHashes;
			this.parse = parse;
			this.isThrowingException = isThrowingException;
			this.fileName = fileName;
		}

		public UploadOptions() {
			this.handleDeletionOfUnreferencedHashes = true;
			this.isThrowingException = false;
			this.fileName = null;
		}

		public UploadOptions(String fileName) {
			this.fileName = fileName;
			this.handleDeletionOfUnreferencedHashes = true;
			this.isThrowingException = false;
		}

		public UploadOptions setHandleDeletionOfUnreferencedHashes(boolean handleDeletionOfUnreferencedHashes) {
			this.handleDeletionOfUnreferencedHashes = handleDeletionOfUnreferencedHashes;
			return this;
		}

		public UploadOptions setParse(Boolean parse) {
			this.parse = parse;
			return this;
		}

		public UploadOptions setThrowingException(boolean throwingException) {
			isThrowingException = throwingException;
			return this;
		}

		public UploadOptions setFileName(String fileName) {
			this.fileName = fileName;
			return this;
		}

		public boolean isHandleDeletionOfUnreferencedHashes() {
			return handleDeletionOfUnreferencedHashes;
		}

		public boolean isParse(boolean defaultBehavior) {
			return parse == null ? defaultBehavior : parse;
		}

		public boolean isThrowingException() {
			return isThrowingException;
		}

		public String getFileName() {
			return fileName;
		}

	}

	public ParsedContentResponse buildParsedContentResponse(boolean hasFoundDuplicate, ParsedContent parsedContent) {
		return new ParsedContentResponse(hasFoundDuplicate, parsedContent);
	}

	public class ParsedContentResponse {
		private boolean hasFoundDuplicate;
		private ParsedContent parsedContent;

		public ParsedContentResponse(boolean hasFoundDuplicate, ParsedContent parsedContent) {
			this.hasFoundDuplicate = hasFoundDuplicate;
			this.parsedContent = parsedContent;
		}

		public boolean hasFoundDuplicate() {
			return hasFoundDuplicate;
		}

		public ParsedContent getParsedContent() {
			return parsedContent;
		}
	}

	public class ContentVersionDataSummaryResponse {
		private boolean hasFoundDuplicate;
		private ContentVersionDataSummary contentVersionDataSummary;

		public ContentVersionDataSummaryResponse(boolean hasFoundDuplicate, ContentVersionDataSummary contentVersionDataSummary) {
			this.hasFoundDuplicate = hasFoundDuplicate;
			this.contentVersionDataSummary = contentVersionDataSummary;
		}

		public boolean hasFoundDuplicate() {
			return hasFoundDuplicate;
		}

		public ContentVersionDataSummary getContentVersionDataSummary() {
			return contentVersionDataSummary;
		}
	}
}