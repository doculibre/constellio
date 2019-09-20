package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.RMSelectedFoldersAndDocumentsSIPBuilder;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.sip.bagInfo.DefaultSIPZipBagInfoFactory;
import com.constellio.app.services.sip.zip.AutoSplittedSIPZipWriter;
import com.constellio.app.services.sip.zip.DefaultSIPFileNameProvider;
import com.constellio.app.services.sip.zip.SIPFileNameProvider;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;

public class SIPBuildAsyncTask implements AsyncTask {

	private static final String UPLOAD_FILE_STREAM_NAME = "SIPBuildAsyncTask-UploadFile";

	private static final long SIP_MAX_FILES_LENGTH = (6 * FileUtils.ONE_GB);

	private String sipFileName;
	private List<String> bagInfoLines;
	private List<String> includeDocumentIds;
	private List<String> includeFolderIds;
	private boolean limitSize;
	private String username;
	private boolean deleteFiles;
	private String currentVersion;
	private UUID uuid;
	private Locale locale;

	public SIPBuildAsyncTask(String sipFileName, List<String> bagInfoLines, List<String> includeDocumentIds,
							 List<String> includeFolderIds, Boolean limitSize, String username, Boolean deleteFiles,
							 String currentVersion) {
		this(sipFileName, bagInfoLines, includeDocumentIds, includeFolderIds, limitSize, username, deleteFiles, currentVersion, "fr");
	}

	public SIPBuildAsyncTask(String sipFileName, List<String> bagInfoLines, List<String> includeDocumentIds,
							 List<String> includeFolderIds, Boolean limitSize, String username, Boolean deleteFiles,
							 String currentVersion,
							 String localeLanguage) {
		this.bagInfoLines = bagInfoLines;
		this.includeDocumentIds = includeDocumentIds;
		this.includeFolderIds = includeFolderIds;
		this.limitSize = limitSize;
		this.username = username;
		this.deleteFiles = deleteFiles;
		this.currentVersion = currentVersion;
		this.uuid = UUID.randomUUID();
		this.locale = Locale.forLanguageTag(localeLanguage);

		this.sipFileName = sipFileName;
		if (sipFileName.toLowerCase().endsWith(".zip") || sipFileName.toLowerCase().endsWith(".sip")) {
			this.sipFileName = StringUtils.substringBeforeLast(sipFileName, ".");
		}

		validateParams();
	}

	@Override
	public void execute(AsyncTaskExecutionParams params)
			throws Exception {

		final AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		IOServices ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		File outFolder = ioServices.newTemporaryFolder("SIPArchives");
		try {
			buildSIPFiles(appLayerFactory, params, outFolder);

			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(params.getCollection(), appLayerFactory);
			uploadSIPFilesInVault(rm, outFolder);
			if (deleteFiles) {
				deleteRecords(appLayerFactory.getModelLayerFactory(), params.getCollection());
			}

		} finally {
			ioServices.deleteQuietly(outFolder);
		}
	}

	private void buildSIPFiles(AppLayerFactory appLayerFactory, final AsyncTaskExecutionParams params, File outFolder)
			throws IOException {
		RMSelectedFoldersAndDocumentsSIPBuilder sipBuilder = new RMSelectedFoldersAndDocumentsSIPBuilder(
				params.getCollection(), appLayerFactory);
		DefaultSIPZipBagInfoFactory bagInfoFactory = new DefaultSIPZipBagInfoFactory(appLayerFactory, locale);
		bagInfoFactory.setHeaderLines(bagInfoLines);

		long zipMaximumLength = limitSize ? SIP_MAX_FILES_LENGTH : 0;
		SIPFileNameProvider sipFileNameProvider = new DefaultSIPFileNameProvider(outFolder, sipFileName);
		AutoSplittedSIPZipWriter writer = new AutoSplittedSIPZipWriter(
				appLayerFactory, sipFileNameProvider, zipMaximumLength, bagInfoFactory);
		sipBuilder.buildWithFoldersAndDocuments(writer, this.includeFolderIds, this.includeDocumentIds, new ProgressInfo() {
			@Override
			public void setEnd(long end) {
				params.setProgressionUpperLimit((int) end);
			}

			long lastCurrentState;

			@Override
			public void setCurrentState(long currentState) {
				params.incrementProgression((int) (currentState - lastCurrentState));
				lastCurrentState = currentState;
			}

		});
	}

	@SuppressWarnings("unchecked")
	protected void deleteRecords(ModelLayerFactory modelLayerFactory, String collection) {
		List<String> ids = ListUtils.union(this.includeDocumentIds, this.includeFolderIds);
		final RecordServices recordServices = modelLayerFactory.newRecordServices();
		User user = modelLayerFactory.newUserServices().getUserInCollection(this.username, collection);
		for (String documentIds : ids) {
			try {
				Record record = recordServices.getDocumentById(documentIds);
				recordServices.logicallyDelete(record, user);
				recordServices.physicallyDelete(record, user,
						new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				e.printStackTrace();
			}
		}
	}

	private void uploadSIPFilesInVault(RMSchemasRecordsServices rm, File outFolder) throws RecordServicesException {

		ModelLayerFactory modelLayerFactory = rm.getModelLayerFactory();
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		User user = modelLayerFactory.newUserServices().getUserInCollection(this.username, rm.getCollection());
		File[] sipFiles = outFolder.listFiles();
		if (sipFiles != null) {
			Transaction transaction = new Transaction();
			for (int i = 1; i <= sipFiles.length; i++) {
				File sipFile = sipFiles[i - 1];

				SIParchive sipArchive = rm.newSIParchive();

				String fileName;
				if (sipFiles.length > 1) {
					fileName = sipFileName + " (" + i + " " + $("SIPBuildAsyncTask.of", locale) + " " + sipFiles.length + ").zip";
				} else {
					fileName = sipFileName + ".zip";
				}
				UploadOptions uploadOptions = new UploadOptions(fileName);
				uploadOptions.setParse(false);

				ContentVersionDataSummary summary;
				InputStream inputStream = ioServices.newBufferedFileInputStreamWithoutExpectableFileNotFoundException(
						sipFile, UPLOAD_FILE_STREAM_NAME);
				try {
					summary = contentManager.upload(inputStream, uploadOptions).getContentVersionDataSummary();

				} finally {
					ioServices.closeQuietly(inputStream);
				}

				sipArchive.setContent(contentManager.createMajor(user, fileName, summary));
				sipArchive.setUser(user);
				sipArchive.setCreatedBy(user.getId());
				sipArchive.setCreationDate(new LocalDateTime(sipFile.lastModified()));
				transaction.add(sipArchive);

				//The containing folder will be deleted later, the file is deleted now to free space on server
				ioServices.deleteQuietly(sipFile);

			}
			modelLayerFactory.newRecordServices().execute(transaction);
		}
	}

	public String getUUID() {
		return uuid.toString();
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[]{sipFileName, bagInfoLines, includeDocumentIds, includeFolderIds, limitSize, username, deleteFiles,
							currentVersion, locale.getLanguage()};
	}


	private void validateParams()
			throws ImpossibleRuntimeException {
		if (this.sipFileName == null || this.sipFileName.isEmpty()) {
			throw new ImpossibleRuntimeException("sip file name null");
		}

		if (this.username == null || this.username.isEmpty()) {
			throw new ImpossibleRuntimeException("username null");
		}

		if (this.currentVersion == null || this.currentVersion.isEmpty()) {
			throw new ImpossibleRuntimeException("version null");
		}
	}
}