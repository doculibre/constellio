package com.constellio.model.services.contents;

import com.constellio.data.conf.HashingEncoding;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.ConversionManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactory;
import com.constellio.data.io.streamFactories.services.one.StreamOperationThrowingException;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.ImageUtils;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.VaultScanReport;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
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
import com.constellio.model.services.contents.thumbnail.ThumbnailGenerator;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.utils.MimeTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.constellio.data.threads.BackgroundThreadConfiguration.repeatingAction;
import static com.constellio.data.utils.dev.Toggle.LOG_CONVERSION_FILENAME_AND_SIZE;
import static com.constellio.model.conf.FoldersLocator.usingAppWrapper;
import static com.constellio.model.entities.enums.ParsingBehavior.ASYNC_PARSING_FOR_ALL_CONTENTS;
import static com.constellio.model.entities.enums.ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static java.util.Arrays.asList;

public class ContentManager implements StatefulService {

	//TODO Increase this limit to 100
	private static final int REPARSE_REINDEX_BATCH_SIZE = 1;
	private static final long FILE_MINIMUM_AGE_BEFORE_DELETION_IN_MILLIS = 259200000; //3 days

	public static final String READ_FILE_TO_UPLOAD = "ContentManager-ReadFileToUpload";

	public static final String READ_CONTENT_FOR_PREVIEW_CONVERSION = "ContentManager-ReadContentForPreviewConversion";

	static final String CONTENT_IMPORT_THREAD = "ContentImportThread";

	static final String DEPRECATED_DAILY_DELETE_UNREFERENCED_CONTENT_BACKGROUND_THREAD = "DeprecatedDailyDeleteUnreferencedContent";

	static final String PARSING_BACKGROUND_THREAD = "ParsingContent";

	static final String WEEKLY_DELETE_UNREFERENCED_CONTENT_BACKGROUND_THREAD = "ScanVaultContents";

	static final String PREVIEW_BACKGROUND_THREAD = "GeneratePreviewContent";

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
	private final ThumbnailGenerator thumbnailGenerator;
	private final DataLayerSystemExtensions dataLayerSystemExtensions;

	private boolean serviceThreadEnabled = true;

	private final ConversionManager conversionManager;

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
		this.thumbnailGenerator = new ThumbnailGenerator();
		this.conversionManager = modelLayerFactory.getDataLayerFactory().getConversionManager();
		this.dataLayerSystemExtensions = modelLayerFactory.getDataLayerFactory().getExtensions().getSystemWideExtensions();
	}

	@Override
	public void initialize() {
		Runnable deprecatedDailyDeleteUnreferencedContentRunnable = new Runnable() {
			@Override
			public void run() {
				if (Toggle.OLD_DELETE_UNUSED_CONTENT_METHOD.isEnabled() && serviceThreadEnabled
					&& ReindexingServices.getReindexingInfos() == null
					&& modelLayerFactory.getConfiguration().isDeleteUnusedContentEnabled()
					&& modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized()) {
					deleteUnreferencedContents();

				}
			}
		};

		Runnable handleRecordsMarkedForParsingRunnable = new Runnable() {
			@Override
			public void run() {
				if (serviceThreadEnabled && ReindexingServices.getReindexingInfos() == null
					&& modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized()
					&& !Toggle.PERFORMANCE_TESTING.isEnabled()) {
					boolean converted = handleRecordsMarkedForParsing();

					if (!converted && usingAppWrapper()) {
						long waitedMinutes = 10;
						if (new ConstellioEIMConfigs(modelLayerFactory).getDefaultParsingBehavior() == ASYNC_PARSING_FOR_ALL_CONTENTS) {
							waitedMinutes = 1;
						}
						try {
							Thread.sleep(waitedMinutes * 60_000);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		};

		Runnable generatePreviewsInBackgroundRunnable = new Runnable() {
			@Override
			public void run() {
				if (serviceThreadEnabled && ReindexingServices.getReindexingInfos() == null
					&& new ConstellioEIMConfigs(modelLayerFactory).isInViewerContentsConversionSchedule()
					&& modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized()) {
					convertPendingContentForPreview();
				}
			}
		};
		Runnable weeklyDeleteUnreferencedContentRunnable = new Runnable() {
			@Override
			public void run() {
				boolean isDeleteUnusedContentEnabled = modelLayerFactory.getConfiguration().isDeleteUnusedContentEnabled();
				boolean isInScanVaultContentsSchedule = new ConstellioEIMConfigs(modelLayerFactory).isInScanVaultContentsSchedule();
				if (serviceThreadEnabled && ReindexingServices.getReindexingInfos() == null
					&& modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized()
					&& isInScanVaultContentsSchedule
					&& isEncodingSafeForScan()
					&& !doesContentScanLockFileExist()
					&& isDeleteUnusedContentEnabled) {
					try {
						createContentScanLockFile();
						VaultScanResults vaultScanResults = new VaultScanResults();
						addReportsAsTemporaryRecords(vaultScanResults);
						scanVaultContentAndDeleteUnreferencedFiles(vaultScanResults);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (RecordServicesException e) {
						e.printStackTrace();
					}
				} else if (!isInScanVaultContentsSchedule && doesContentScanLockFileExist()) {
					deleteContentScanLockFile();
				}

			}
		};

		backgroundThreadsManager.configure(
				repeatingAction(DEPRECATED_DAILY_DELETE_UNREFERENCED_CONTENT_BACKGROUND_THREAD, deprecatedDailyDeleteUnreferencedContentRunnable)
						.executedEvery(
								configuration.getParsingContentsThreadDelayBetweenChecks())
						.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE));

		backgroundThreadsManager.configure(
				repeatingAction(PARSING_BACKGROUND_THREAD, handleRecordsMarkedForParsingRunnable)
						.executedEvery(configuration.getParsingContentsThreadDelayBetweenChecks())
						.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE));

		backgroundThreadsManager.configure(
				repeatingAction(PREVIEW_BACKGROUND_THREAD, generatePreviewsInBackgroundRunnable)
						.executedEvery(
								configuration.getGeneratePreviewsThreadDelayBetweenChecks())
						.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE));

		backgroundThreadsManager.configure(
				repeatingAction(WEEKLY_DELETE_UNREFERENCED_CONTENT_BACKGROUND_THREAD, weeklyDeleteUnreferencedContentRunnable)
						.executedEvery(Duration.standardHours(2))
						.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE));

		if (configuration.getContentImportThreadFolder() != null) {
			Runnable contentImportAction = new Runnable() {

				@Override
				public void run() {
					uploadFilesInImportFolder();
				}
			};
			backgroundThreadsManager.configure(
					repeatingAction(CONTENT_IMPORT_THREAD, contentImportAction)
							.executedEvery(Duration.standardSeconds(30))
							.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE));
		}

		//
		icapService.init();
	}

	private void addReportsAsTemporaryRecords(VaultScanResults vaultScanResults) throws RecordServicesException {
		List<String> collectionList = collectionsListManager.getCollectionsExcludingSystem();
		ArrayList<VaultScanReport> reportList = new ArrayList<>();
		for (String collection : collectionList) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
			reportList.add(schemas.newVaultScanReport());
		}

		//TODO replace text for content
		for (VaultScanReport report : reportList) {
			report.set(VaultScanReport.MESSAGE, vaultScanResults.getReportMessage());
			report.set(VaultScanReport.NUMBER_OF_DELETED_CONTENTS, vaultScanResults.getNumberOfDeletedContents());
			recordServices.add(report);
		}
	}

	private void createContentScanLockFile() throws IOException {
		File confFolder = modelLayerFactory.getFoldersLocator().getConfFolder();
		File lockFile = new File(confFolder, "contentScan.lock");

		if (!lockFile.exists()) {
			lockFile.createNewFile();
		}
	}

	private boolean doesContentScanLockFileExist() {
		File confFolder = modelLayerFactory.getFoldersLocator().getConfFolder();
		File lockFile = new File(confFolder, "contentScan.lock");
		return lockFile.exists();
	}

	private void deleteContentScanLockFile() {
		File confFolder = modelLayerFactory.getFoldersLocator().getConfFolder();
		File lockFile = new File(confFolder, "contentScan.lock");
		ioServices.deleteQuietly(lockFile);
	}

	public void scanVaultContentAndDeleteUnreferencedFiles(VaultScanResults vaultScanResults) {
		List<String> vaultContentFileList = new ArrayList<>();
		getAllContentsFromVaultAndRemoveOrphan("", vaultContentFileList, vaultScanResults);
		recordServices.flushRecords();
		Set<String> allReferencedHashes = getAllReferencedHashes();

		ContentDao contentDao = getContentDao();
		for (String fileId : vaultContentFileList) {
			if (!allReferencedHashes.contains(fileId)) {
				File file = contentDao.getFileOf(fileId);
				if (file.exists()) {
					LocalDateTime lastModification = contentDao.getLastModification(fileId);
					if (lastModification.isBefore(TimeProvider.getLocalDateTime().minus(configuration.getDelayBeforeDeletingUnreferencedContents()))) {
						try {
							contentDao.delete(asList(fileId, fileId + "__parsed", fileId + ".preview", fileId + ".jpegConversion", fileId + ".icapscan", fileId + ".thumbnail"));
							contentDao.deleteFileNameContaining(fileId, ".annotation.");

							vaultScanResults.incrementNumberOfDeletedContents();
							vaultScanResults.appendMessage("INFO: Successfully deleted file " + file.getName() + "\n");
						} catch (Exception e) {
							vaultScanResults.appendMessage("ERROR: Could not delete file " + file.getName() + "\n");
							vaultScanResults.appendMessage(e.getMessage() + "\n");
						}
					}
				}
			}
		}
	}

	public void getAllContentsFromVaultAndRemoveOrphan(String folderId, List<String> fileList,
													   VaultScanResults vaultScanResults) {
		ContentDao contentDao = getContentDao();
		List<String> subFiles = contentDao.getFolderContents(folderId);
		for (String fileId : subFiles) {
			File file = contentDao.getFileOf(fileId);
			if (file.exists() && shouldFileBeScannedForDeletion(file)) {
				if (file.isDirectory()) {
					getAllContentsFromVaultAndRemoveOrphan(fileId, fileList, vaultScanResults);
				} else if (new AgeFileFilter(TimeProvider.getLocalDateTime().toDate().getTime() - FILE_MINIMUM_AGE_BEFORE_DELETION_IN_MILLIS).accept(file)) {
					fileList.add(file.getName());
				}
			}
		}
		if (vaultScanResults != null) {
			deleteOrphanParsedContentOrPreview(subFiles, vaultScanResults);
		}
	}

	private void deleteOrphanParsedContentOrPreview(List<String> subFiles, VaultScanResults vaultScanResults) {
		ContentDao contentDao = getContentDao();
		for (String fileId : subFiles) {
			File file = contentDao.getFileOf(fileId);
			if (file.exists() && (file.getName().endsWith("__parsed") || file.getName().endsWith(".preview") || file.getName().endsWith(".jpegConversion")
								  || file.getName().endsWith(".icapscan") || file.getName().contains(".annotation."))) {
				File mainFile = getMainFile(file);
				if (!mainFile.exists()) {
					try {
						contentDao.delete(asList(fileId));
						vaultScanResults.incrementNumberOfDeletedContents();
						vaultScanResults.appendMessage("INFO: Successfully deleted file " + file.getName() + "\n");
					} catch (Exception e) {
						vaultScanResults.appendMessage("ERROR: Could not delete file " + file.getName() + "\n");
						vaultScanResults.appendMessage(e.getMessage() + "\n");
					}
				}
			}
		}
	}

	private File getMainFile(File parsedContentOrPreviewFile) {
		String parsedContentOrPreviewFileAbsolutePath = parsedContentOrPreviewFile.getAbsolutePath();
		String mainFileAbsolutePath = StringUtils.removeEnd(parsedContentOrPreviewFileAbsolutePath, "__parsed");

		if (parsedContentOrPreviewFileAbsolutePath.equals(mainFileAbsolutePath)) {
			if (parsedContentOrPreviewFile.getName().contains(".annotation.")) {
				String absolutePath = parsedContentOrPreviewFile.getAbsolutePath();

				int indexOfAnnotation = absolutePath.indexOf(".annotation.");
				mainFileAbsolutePath = absolutePath.substring(0, indexOfAnnotation);
			} else {
				mainFileAbsolutePath = StringUtils.removeEnd(parsedContentOrPreviewFileAbsolutePath, ".preview");
			}
		}



		return new File(mainFileAbsolutePath);
	}

	private boolean shouldFileBeScannedForDeletion(File file) {
		boolean isMainFile = !(file.getName().endsWith("__parsed") || file.getName().endsWith(".preview")
							   || file.getName().endsWith(".thumbnail") || file.getName().endsWith(".jpegConversion")
							   || file.getName().endsWith(".icapscan") || file.getName().contains(".annotation."));
		List<String> restrictedFiles = asList("tlogs", "tlogs_bck", "vaultRecovery");
		return isMainFile && !restrictedFiles.contains(file.getName());
	}

	public ContentManager setServiceThreadEnabled(boolean serviceThreadEnabled) {
		this.serviceThreadEnabled = serviceThreadEnabled;
		return this;
	}

	@Override
	public void close() {
		try {
			closing.set(true);
		} finally {
			conversionManager.close();
		}
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

	public Content createEmptyMajor(User user, String filename, ContentVersionDataSummary newVersion) {
		String uniqueId = uniqueIdGenerator.next();
		return ContentImpl.create(uniqueId, user, filename, newVersion, true, true);
	}

	public Content createSystemContent(String filename, ContentVersionDataSummary version) {
		return ContentImpl.createSystemContent(filename, version);
	}

	public boolean hasContentPreview(String hash) {
		return getContentDao().isDocumentExisting(hash + ".preview");
	}

	public boolean hasContentAnnotation(String hash, String id, String version) {
		return getContentDao().isDocumentExisting(hash + ".annotation." + id + "." + version);
	}

	public boolean hasContentThumbnail(String hash) {
		return getContentDao().isDocumentExisting(hash + ".thumbnail");
	}

	public boolean hasContentJpegConversion(String hash) {
		return getContentDao().isDocumentExisting(hash + ".jpegConversion");
	}

	public boolean doesFileExist(String hash) {
		return getContentDao().isDocumentExisting(hash);
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

	public InputStream getContentAnnotationInputStream(String hash, String id, String version, String streamName) {
		try {
			return getContentDao().getContentInputStream(hash + ".annotation." + id + "." + version, streamName);
		} catch (ContentDaoException_NoSuchContent e) {
			throw new ContentManagerRuntimeException.ContentManagerRuntimeException_ContentHasNoAnnotation(hash);
		}
	}

	public InputStream getContentThumbnailInputStream(String hash, String streamName) {
		try {
			return getContentDao().getContentInputStream(hash + ".thumbnail", streamName);
		} catch (ContentDaoException_NoSuchContent e) {
			throw new ContentManagerRuntimeException.ContentManagerRuntimeException_ContentHasNoThumbnail(hash);
		}
	}

	public InputStream getContentJpegConversionInputStream(String hash, String streamName) {
		try {
			return getContentDao().getContentInputStream(hash + ".jpegConversion", streamName);
		} catch (ContentDaoException_NoSuchContent e) {
			throw new ContentManagerRuntimeException.ContentManagerRuntimeException_ContentHasNoJpegConversion(hash);
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

	public ContentVersionDataSummary upload(File file)
			throws FileNotFoundException {

		InputStream inputStream = ioServices.newBufferedFileInputStream(file, READ_FILE_TO_UPLOAD);

		try {
			return upload(inputStream, new UploadOptions(file.getName())).getContentVersionDataSummary();
		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	@Deprecated
	public ContentVersionDataSummary upload(InputStream inputStream) {
		return upload(inputStream, new UploadOptions()).getContentVersionDataSummary();
	}

	public ContentVersionDataSummaryResponse upload(InputStream inputStream, String filename) {
		return upload(inputStream, new UploadOptions(filename));
	}

	public ContentVersionDataSummaryResponse upload(InputStream inputStream, UploadOptions uploadOptions) {
		String fileName = uploadOptions.getFileName();
		boolean handleDeletionOfUnreferencedHashes = uploadOptions.isHandleDeletionOfUnreferencedHashes();

		boolean defaultParsing = modelLayerFactory.getSystemConfigs()
										 .getDefaultParsingBehavior() == SYNC_PARSING_FOR_ALL_CONTENTS;

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
			ParsedContent parsedContent = null;
			if (parse) {
				ParseOptions options = uploadOptions.parseOptions != null ? uploadOptions.parseOptions : new ParseOptions();
				ParsedContentResponse parsedContentResponse =
						getPreviouslyParsedContentOrParseFromStream(hash, closeableInputStreamFactory, options);
				parsedContent = parsedContentResponse.getParsedContent();
				mimeType = parsedContent.getMimeType();
				if (mimeType == null) {
					mimeType = detectMimetype(closeableInputStreamFactory, fileName);
				}
				duplicate = parsedContentResponse.hasFoundDuplicate();
			} else {
				mimeType = detectMimetype(closeableInputStreamFactory, fileName);
				duplicate = doesFileExist(hash);
			}

			//saveContent(hash, closeableInputStreamFactory);
			long length = closeableInputStreamFactory.length();
			saveContent(hash, closeableInputStreamFactory);
			return new ContentVersionDataSummaryResponse(duplicate,
					new ContentVersionDataSummary(hash, mimeType, length), parsedContent);

		} catch (HashingServiceException | IOException e) {
			throw new ContentManagerRuntimeException_CannotReadInputStream(e);

		} finally {
			ioServices.closeQuietly(closeableInputStreamFactory);
		}
	}

	public ContentVersionDataSummaryResponse getContentVersionSummary(String hash) {
		ParsedContentResponse parsedContentResponse = getParsedContentParsingIfNotYetDone(hash);
		ParsedContent parsedContent = parsedContentResponse.getParsedContent();
		return new ContentVersionDataSummaryResponse(parsedContentResponse.hasFoundDuplicate(),
				new ContentVersionDataSummary(hash, parsedContent.getMimeType(), parsedContent.getLength()),
				parsedContent);
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
		return getPreviouslyParsedContentOrParseFromStream(hash, inputStreamFactory, new ParseOptions());
	}

	ParsedContentResponse getPreviouslyParsedContentOrParseFromStream(String hash,
																	  CloseableStreamFactory<InputStream> inputStreamFactory,
																	  ParseOptions parseOptions)
			throws IOException {

		ParsedContent parsedContent;
		ParsedContentResponse response;
		try {
			parsedContent = getParsedContent(hash);
			response = new ParsedContentResponse(true, parsedContent);
		} catch (ContentManagerException_ContentNotParsed e) {
			parsedContent = parseAndSave(hash, inputStreamFactory, parseOptions);
			response = new ParsedContentResponse(false, parsedContent);
		}
		return response;
	}

	private ParsedContent parseAndSave(String hash, CloseableStreamFactory<InputStream> inputStreamFactory,
									   ParseOptions options)
			throws IOException {
		ParsedContent parsedContent = tryToParse(inputStreamFactory, options);
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

	ParsedContent tryToParse(CloseableStreamFactory<InputStream> inputStreamFactory, ParseOptions options)
			throws IOException {
		try {
			return fileParser.parse(inputStreamFactory, inputStreamFactory.length(), options);

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

		if (hash != null) {
			String id = UUIDV1Generator.newRandomId();
			Map<String, Object> fields = new HashMap<>();
			fields.put("type_s", "marker");
			fields.put("contentMarkerHash_s", hash);
			fields.put("time_dt", TimeProvider.getLocalDateTime());

			RecordDTO recordDTO = new SolrRecordDTO(id, fields, RecordDTOMode.FULLY_LOADED);
			try {
				recordDao.execute(new TransactionDTO(RecordsFlushing.ADD_LATER()).withNewRecords(asList(recordDTO)));
			} catch (OptimisticLocking e) {
				throw new ImpossibleRuntimeException(e);
			}
		}
	}

	private Set<String> getAllReferencedHashes() {
		Set<String> referencedHashes = new HashSet<>();
		List<String> collections = collectionsListManager.getCollections();
		for (String collection : collections) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			List<MetadataSchemaType> metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();
			for (final MetadataSchemaType metadataSchemaType : metadataSchemaTypeList) {
				List<Metadata> contentMetadatas = new ArrayList<>();
				for (Metadata metadata : metadataSchemaType.getAllMetadatas()) {
					if (MetadataValueType.CONTENT.equals(metadata.getType())) {
						contentMetadatas.add(metadata);
					}
				}

				if (!contentMetadatas.isEmpty()) {
					referencedHashes.addAll(findReferencedContentsForSchemaType(metadataSchemaType, contentMetadatas));
				}
			}
		}
		return referencedHashes;
	}

	private Set<String> findReferencedContentsForSchemaType(MetadataSchemaType metadataSchemaType,
															List<Metadata> contentMetadatas) {
		Set<String> referencedHashes = new HashSet<>();
		LogicalSearchCondition condition = from(metadataSchemaType).where(contentMetadatas.get(0)).isNotNull();
		for (int i = 1; i < contentMetadatas.size(); i++) {
			condition = condition.orWhere(contentMetadatas.get(i)).isNotNull();
		}

		SearchResponseIterator<Record> recordsIterator =
				searchServices.recordsIterator(new LogicalSearchQuery(condition)
						.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(contentMetadatas)));
		while (recordsIterator.hasNext()) {
			Record record = recordsIterator.next();
			for (Metadata metadata : contentMetadatas) {
				MetadataSchema recordSchema = metadataSchemaType.getSchema(record.getSchemaCode());

				if (recordSchema.hasMetadataWithCode(metadata.getLocalCode())
					&& MetadataValueType.CONTENT.equals(recordSchema.getMetadata(metadata.getLocalCode()).getType())) {
					for (Content recordContent : record.<Content>getValues(metadata)) {
						ContentImpl content = (ContentImpl) recordContent;
						referencedHashes.addAll(content.getHashOfAllVersions());
					}
				}
			}
		}
		return referencedHashes;
	}

	boolean isEncodingSafeForScan() {
		HashingEncoding hashingEncoding = modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration().getHashingEncoding();
		switch (hashingEncoding) {
			case BASE64_URL_ENCODED:
			case BASE32:
				return true;
			default:
				return false;
		}
	}

	boolean isReferenced(File file) {
		HashingEncoding hashingEncoding = modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration().getHashingEncoding();
		switch (hashingEncoding) {
			case BASE64_URL_ENCODED:
			case BASE32:
				return isReferenced(file.getName());
			default:
				return true;
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

	public boolean convertPendingContentForPreview() {
		boolean converted = false;
		if (!Toggle.PERFORMANCE_TESTING.isEnabled() && Toggle.CONTENT_CONVERSION.isEnabled()) {
			for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
				if (!closing.get()) {
					List<Record> records = searchServices.search(new LogicalSearchQuery()
							.setCondition(fromAllSchemasIn(collection).where(Schemas.MARKED_FOR_PREVIEW_CONVERSION).isTrue())
							.setNumberOfRows(20).sortDesc(Schemas.MODIFIED_ON).sortDesc(Schemas.CREATED_ON)
							.setName("ContentManager:BackgroundThread:convertPendingContentForPreview()"));

					if (!records.isEmpty()) {
						converted = true;
						final File tempFolder = ioServices.newTemporaryFolder("previewConversion");
						try {
							Transaction transaction = new Transaction();
							transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions()
									.setOverwriteModificationDateAndUser(false));
							for (Record record : records) {
								if (!closing.get()) {
									try {
										tryConvertRecordContents(record, tempFolder);
									} catch (Throwable t) {
										t.printStackTrace();
										//unsupported extension
									} finally {
										transaction.add(record.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, null));
									}
								}
							}
							try {
								transaction.setRecordFlushing(RecordsFlushing.WITHIN_SECONDS(5));
								transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
								transaction.getRecordUpdateOptions().setUpdateCalculatedMetadatas(false);
								recordServices.executeWithoutImpactHandling(transaction);
							} catch (RecordServicesException.OptimisticLocking e) {
								LOGGER.trace("Optimistic locking occured with record " + e.getId());

							} catch (RecordServicesException e) {
								throw new RuntimeException(e);
							}

						} finally {
							ioServices.deleteQuietly(tempFolder);
						}
					}
				}
			}
		}
		return converted;
	}

	private boolean tryConvertRecordContents(Record record, File tempFolder) {
		MetadataSchema schema = metadataSchemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
		boolean isConversionSuccessful = true;
		for (Metadata contentMetadata : schema.getMetadatas().onlyWithType(MetadataValueType.CONTENT)) {
			if (!contentMetadata.isMultivalue()) {
				Content content = record.get(contentMetadata);

				if (content != null && !isExtentionIsDisabledForPreview(content)) {
					isConversionSuccessful = isConversionSuccessful &&
											 tryConvertContentForPreview(content, tempFolder);
				}
			}
		}

		return isConversionSuccessful;
	}

	private boolean isExtentionIsDisabledForPreview(Content content) {
		String fileName = content.getCurrentVersion().getFilename();

		String ext = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));

		return asList(this.dataLayerSystemExtensions.getExtentionDisabledForPreviewConvertion()).contains(ext);
	}

	private boolean tryConvertContentForPreview(Content content, File tempFolder) {
		String hash = content.getCurrentVersion().getHash();
		String filename = content.getCurrentVersion().getFilename();
		String mimeType = content.getCurrentVersion().getMimetype();
		ContentDao contentDao = getContentDao();

		if (mimeType.startsWith("image/")) {
			Dimension dimension = ImageUtils.getImageDimension(contentDao.getFileOf(hash));

			boolean tiffSupported = modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration().areTiffFilesConvertedForPreview();
			if (((tiffSupported && mimeType.equals(MimeTypes.MIME_IMAGE_TIFF)) || dimension.getHeight() > 1080) &&
				!contentDao.isDocumentExisting(hash + ".jpegConversion")) {
				try (InputStream inputStream = contentDao.getContentInputStream(hash, READ_CONTENT_FOR_PREVIEW_CONVERSION)) {
					File convertedJPEGFile =
							conversionManager.convertToJPEG(inputStream, dimension, mimeType, filename, tempFolder);
					contentDao.moveFileToVault(convertedJPEGFile, hash + ".jpegConversion");
					LOGGER.info("Generated a JPEG conversion for content '" + filename + "' with hash '" + hash + "'");
				} catch (Throwable t) {
					LOGGER.warn("Cannot generate JPEG conversion for content '" + filename + "' with hash '" + hash + "'", t);
					return false;
				}
			}
		} else if (!mimeType.startsWith("application/pdf") && !contentDao.isDocumentExisting(hash + ".preview")) {
			try (InputStream inputStream = contentDao.getContentInputStream(hash, READ_CONTENT_FOR_PREVIEW_CONVERSION)) {
				if (LOG_CONVERSION_FILENAME_AND_SIZE.isEnabled()) {
					LOGGER.info("Converting file " + filename + " : " + content.getCurrentVersion().getLength() / (1024 * 1024));
				}
				File pdfPreviewFile = conversionManager.convertToPDF(inputStream, filename, tempFolder);
				contentDao.moveFileToVault(pdfPreviewFile, hash + ".preview");

			} catch (ContentDaoException_NoSuchContent e) {
				//Missing content are not logged from the conversion thread. Instead, they are checked by system diagnostic and other mechanisms
				return false;

			} catch (Throwable t) {
				LOGGER.warn("Cannot generate preview for content '" + filename + "' with hash '" + hash + "'", t);
				return false;
			}
		}

		if (new ConstellioEIMConfigs(modelLayerFactory).isThumbnailGenerationEnabled() &&
			!contentDao.isDocumentExisting(hash + ".thumbnail")) {
			try {
				generateThumbnail(contentDao, hash, mimeType, filename, tempFolder);
				LOGGER.info("Generated a thumbnail for content '" + filename + "' with hash '" + hash + "'");
			} catch (Throwable t) {
				LOGGER.warn("Cannot generate thumbnail for content '" + filename + "' with hash '" + hash + "'", t);
				return false;
			}
		}
		return true;
	}

	private void generateThumbnail(ContentDao contentDao, String hash, String mimeType, String filename,
								   File tempFolder) throws Exception {
		InputStream thumbnailSourceInputStream = null;
		try {
			if (mimeType.startsWith("image/") || mimeType.startsWith("application/pdf")) {
				thumbnailSourceInputStream = contentDao.getContentInputStream(hash, READ_CONTENT_FOR_PREVIEW_CONVERSION);
			} else {
				thumbnailSourceInputStream = contentDao.getContentInputStream(hash + ".preview", READ_CONTENT_FOR_PREVIEW_CONVERSION);
				mimeType = MimeTypes.MIME_APPLICATION_PDF;
			}

			BufferedImage thumbnailImage = thumbnailGenerator.generate(thumbnailSourceInputStream, mimeType);
			String tempFilename = tempFolder + filename + "_thumbnail_" + ".png";
			File thumbnailFile = new File(tempFilename);
			ImageIO.write(thumbnailImage, "png", thumbnailFile);
			contentDao.moveFileToVault(thumbnailFile, hash + ".thumbnail");
		} finally {
			ioServices.closeQuietly(thumbnailSourceInputStream);
		}
	}


	public boolean handleRecordsMarkedForParsing() {
		Set<String> collections = getCollectionsWithFlag(Schemas.MARKED_FOR_PARSING);
		boolean converted = false;
		for (String collection : collections) {
			MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
			LogicalSearchQuery query = new LogicalSearchQuery();
			query.setCondition(fromAllSchemasIn(collection).where(Schemas.MARKED_FOR_PARSING).isTrue());
			query.setNumberOfRows(50);
			query.sortDesc(Schemas.MODIFIED_ON);
			query.sortDesc(Schemas.CREATED_ON);
			query.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR);
			query.setName("ContentManager:BackgroundThread:getRecordsWithFlag(markedForParsing_s)");

			List<Record> records = searchServices.search(query);

			if (!records.isEmpty()) {
				Transaction tx = new Transaction();
				tx.addAll(records);
				for (Record record : records) {
					MetadataSchema schema = types.getSchemaOf(record);
					for (Metadata metadata : schema.getContentMetadatasForPopulate()) {

						for (Content content : record.<Content>getValues(metadata)) {
							String hash = content.getCurrentVersion().getHash();
							if (!isParsed(hash)) {
								try {
									parseAndSave(hash, getContentInputStreamFactory(hash), new ParseOptions());
								} catch (IOException | ContentManagerRuntimeException_NoSuchContent e) {
									//TODO
								}
							}

						}

					}
					record.set(Schemas.MARKED_FOR_PARSING, null);
				}

				tx.setOptions(RecordUpdateOptions.validationExceptionSafeOptions().setOverwriteModificationDateAndUser(false));
				tx.getRecordUpdateOptions().setFullRewrite(true);
				try {
					recordServices.execute(tx);
				} catch (RecordServicesException e) {
					//TODO
					throw new RuntimeException(e);
				}
				converted = true;
			}
		}

		return converted;
	}

	private Set<String> getCollectionsWithFlag(Metadata... flags) {

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(fromEveryTypesOfEveryCollection().whereAny(flags).isTrue());
		query.setNumberOfRows(0);
		query.addFieldFacet("collection_s");

		StringBuilder flagsName = new StringBuilder();
		for (Metadata metadata : flags) {
			if (flagsName.length() > 0) {
				flagsName.append(", ");
			}
			flagsName.append(metadata.getDataStoreCode());
		}

		query.setName("ContentManager:BackgroundThread:getCollectionsWithFlag(" + flagsName.toString() + ")");
		SPEQueryResponse response = searchServices.query(query);

		Set<String> collections = new HashSet<>();
		collections.addAll(response.getFieldFacetValuesWithResults("collection_s"));
		return collections;
	}

	public void deleteUnreferencedContents() {

		if (new ConstellioEIMConfigs(modelLayerFactory).isInUnreferencedContentsDeleteSchedule()) {
			deleteUnreferencedContents(RecordsFlushing.NOW());
		}
	}

	public void deleteUnreferencedContents(RecordsFlushing recordsFlushing) {
		List<RecordDTO> potentiallyDeletableContentMarkers;

		while (!(potentiallyDeletableContentMarkers = getNextPotentiallyUnreferencedContentMarkers()).isEmpty()) {

			for (RecordDTO marker : potentiallyDeletableContentMarkers) {
				if (closing.get()) {
					return;
				}
				List<String> hashToDelete = new ArrayList<>();
				String hash = marker.getFields().get("contentMarkerHash_s").toString();
				boolean isNotReferenced = !isReferenced(hash);
				if (isNotReferenced) {
					hashToDelete.add(hash);
					hashToDelete.add(hash + "__parsed");
					hashToDelete.add(hash + ".preview");
					hashToDelete.add(hash + ".thumbnail");
					hashToDelete.add(hash + ".jpegConversion");
					getContentDao().deleteFileNameContaining(hash, ".annotation.");
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

	public String getParsedContentFileName(String hash) {
		return hash + "__parsed";
	}

	public ParsedContent getParsedContent(String hash)
			throws ContentManagerException_ContentNotParsed {
		String parsedContent;

		InputStream inputStream = null;

		try {
			inputStream = getContentDao().getContentInputStream(getParsedContentFileName(hash), READ_PARSED_CONTENT);
			parsedContent = ioServices.readStreamToString(inputStream);
		} catch (ContentDaoException.ContentDaoException_NoSuchContent e) {
			throw new ContentManagerException_ContentNotParsed(hash);

		} catch (IOException e) {
			throw new ContentManagerRuntimeException_CannotReadInputStream(e);

		} finally {
			ioServices.closeQuietly(inputStream);
		}

		if (org.apache.commons.lang3.StringUtils.isBlank(parsedContent)) {
			throw new ContentManagerException_ContentNotParsed(hash);
		}

		try {
			return newParsedContentConverter().convertToParsedContent(parsedContent);
		} catch (Exception e) {
			ContentManagerRuntimeException_CannotReadParsedContent exception
					= new ContentManagerRuntimeException_CannotReadParsedContent(e, hash, parsedContent);
			LOGGER.error(exception.getMessage(), exception);

			return ParsedContent.unparsable("", parsedContent == null ? 0 : parsedContent.length());
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

		return recordDao.searchQuery("ContentManager:BackgroundThread:getNextPotentiallyUnreferencedContentMarkers()", params);
	}

	public void reparse(String hash) {
		CloseableStreamFactory<InputStream> streamFactory = getContentInputStreamFactory(hash);
		try {
			parseAndSave(hash, streamFactory, new ParseOptions());
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
						MetadataSchema schema = types.getSchemaOf(record);
						for (Metadata contentMetadata : contentMetadatas) {
							if (schema.hasMetadataWithCode(contentMetadata.getLocalCode())) {
								record.markAsModified(contentMetadata);
							}
						}
					}
					Transaction transaction = new Transaction(batch);
					transaction.setSkippingRequiredValuesValidation(true);
					transaction.getRecordUpdateOptions().setOverwriteModificationDateAndUser(false);
					try {
						recordServices.execute(transaction);
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}

			}

		}
	}

	public HashingService getHashingService() {
		return hashingService;
	}

	public static class UploadOptions {
		private boolean handleDeletionOfUnreferencedHashes;
		private ParseOptions parseOptions;
		private Boolean ocr;
		private boolean isThrowingException;
		private String fileName;

		public UploadOptions(boolean handleDeletionOfUnreferencedHashes, ParseOptions parseOptions,
							 boolean isThrowingException, String fileName) {
			this.handleDeletionOfUnreferencedHashes = handleDeletionOfUnreferencedHashes;
			this.parseOptions = parseOptions;
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

		public UploadOptions setParseOptions(ParseOptions parseOptions) {
			this.parseOptions = parseOptions;
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
			return handleDeletionOfUnreferencedHashes && Toggle.OLD_DELETE_UNUSED_CONTENT_METHOD.isEnabled();
		}

		public boolean isParse(boolean defaultBehavior) {
			return parseOptions != null ? parseOptions.isParse() : defaultBehavior;
		}

		public boolean isThrowingException() {
			return isThrowingException;
		}

		public String getFileName() {
			return fileName;
		}

		public static UploadOptions asFastAsPossible() {
			return new UploadOptions().setHandleDeletionOfUnreferencedHashes(false);
		}
	}

	@Getter
	@AllArgsConstructor
	public static class ParseOptions {
		private boolean beautify;
		private boolean detectLanguage;
		private boolean ocr;
		private boolean parse;

		public ParseOptions() {
			beautify = true;
			detectLanguage = true;
			parse = true;
			ocr = false;
		}

		public static ParseOptions NO_PARSING = new ParseOptions(false, false, false, false);

		public static ParseOptions PARSING_WITHOUT_OCR = new ParseOptions(true, true, false, true);

		public static ParseOptions PARSING_WITH_OCR = new ParseOptions(true, true, true, true);

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
		@Getter private ParsedContent parsedContent;

		public ContentVersionDataSummaryResponse(boolean hasFoundDuplicate,
												 ContentVersionDataSummary contentVersionDataSummary,
												 ParsedContent parsedContent) {
			this.hasFoundDuplicate = hasFoundDuplicate;
			this.contentVersionDataSummary = contentVersionDataSummary;
			this.parsedContent = parsedContent;
		}

		public boolean hasFoundDuplicate() {
			return hasFoundDuplicate;
		}

		public ContentVersionDataSummary getContentVersionDataSummary() {
			return contentVersionDataSummary;
		}
	}

	public List<File> scanVaultContentAndFindUnreferenced() {
		List<String> vaultContentFileList = new ArrayList<>();
		getAllContentsFromVaultAndRemoveOrphan("", vaultContentFileList, null);
		recordServices.flushRecords();
		Set<String> allReferencedHashes = getAllReferencedHashes();

		ContentDao contentDao = getContentDao();
		List<File> files = new ArrayList<>();
		for (String fileId : vaultContentFileList) {
			if (!allReferencedHashes.contains(fileId)) {
				File file = contentDao.getFileOf(fileId);
				if (file.exists()) {
					files.add(file);
				}
			}
		}

		return files;
	}

	public Stream<VaultContentEntry> stream() {
		Stream<Path> pathStream = modelLayerFactory.getContentManager().getContentDao().streamVaultContent((path) -> {

			if (path.toFile().isDirectory()) {
				return false;

			} else {
				String filename = path.toFile().getName();
				if (path.endsWith("tlogs") || path.getParent().endsWith("tlogs")
					|| filename.endsWith("tlogs-backup")) {
					return false;

				} else if (filename.endsWith(".preview") || filename.endsWith(".thumbnails")
						   || filename.endsWith("__parsed") || filename.endsWith(".jpegConversion")) {
					return false;

				} else {
					return true;
				}

			}
		});

		return pathStream.map((p -> {
			String hash = p.toFile().getName();
			return new VaultContentEntry(p.toFile()) {
				@Override
				public Optional<ParsedContent> loadParsedContent() {
					try {
						return Optional.of(ContentManager.this.getParsedContent(hash));
					} catch (ContentManagerException_ContentNotParsed contentManagerException_contentNotParsed) {
						return Optional.empty();
					}
				}
			};
		}));
	}

	;

	protected static class VaultScanResults {
		private StringBuilder reportMessage = new StringBuilder();
		private int numberOfDeletedContents = 0;

		public void appendMessage(String message) {
			reportMessage.append(message);
		}

		public String getReportMessage() {
			return reportMessage.toString();
		}

		public void incrementNumberOfDeletedContents() {
			numberOfDeletedContents++;
		}

		public int getNumberOfDeletedContents() {
			return numberOfDeletedContents;
		}
	}
}