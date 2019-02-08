package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.RMSIPBuilder;
import com.constellio.app.services.sip.SIPBuilderParams;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SIPBuildAsyncTask implements AsyncTask {

	private static final long SIP_MAX_FILES_LENGTH = (6 * FileUtils.ONE_GB);
	private static final int SIP_MAX_FILES = 9000;

	private String sipFileName;
	private List<String> bagInfoLines;
	private List<String> includeDocumentIds;
	private List<String> includeFolderIds;
	private boolean limitSize;
	private String username;
	private boolean deleteFiles;
	private String currentVersion;
	private ProgressInfo progressInfo;
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
		this.sipFileName = sipFileName;
		this.limitSize = limitSize;
		this.username = username;
		this.deleteFiles = deleteFiles;
		this.currentVersion = currentVersion;
		this.uuid = UUID.randomUUID();
		this.progressInfo = new ProgressInfo();
		this.locale = Locale.forLanguageTag(localeLanguage);
		validateParams();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(AsyncTaskExecutionParams params)
			throws ImpossibleRuntimeException {
		ValidationErrors errors = new ValidationErrors();
		List<String> ids = ListUtils.union(this.includeDocumentIds, this.includeFolderIds);
		if (ids.isEmpty()) {
			errors.add(SIPGenerationValidationException.class, "Lists cannot be null");
		} else {
			final AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			String collection = params.getCollection();
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			File outFolder = null;
			File outFile = null;
			try {

				User currentUser = modelLayerFactory.newUserServices().getUserInCollection(this.username, collection);

				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
				outFolder = modelLayerFactory.getIOServicesFactory().newIOServices().newTemporaryFolder("SIPArchives");
				outFile = new File(outFolder, this.sipFileName);

				final Iterator<String> idsIterator = ids.iterator();
				final RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
				Iterator<Record> recordsIterator = new LazyIterator<Record>() {
					@Override
					protected Record getNextOrNull() {
						return idsIterator.hasNext() ? recordServices.getDocumentById(idsIterator.next()) : null;
					}
				};

				SIPBuilderParams builderParams = new SIPBuilderParams()
						.setProvidedBagInfoHeaderLines(bagInfoLines)
						.setLocale(locale)
						.setSipBytesLimit(limitSize ? SIP_MAX_FILES_LENGTH : 0)
						.setSipFilesLimit(limitSize ? SIP_MAX_FILES : 0);

				RMSIPBuilder constellioSIP = new RMSIPBuilder(collection, appLayerFactory);
				constellioSIP.buildWithFoldersAndDocuments(outFile, this.includeFolderIds, this.includeDocumentIds, progressInfo, builderParams);

				//Create SIParchive record
				ContentManager contentManager = modelLayerFactory.getContentManager();
				SIParchive sipArchive = rm.newSIParchive();
				ContentVersionDataSummary summary = contentManager.upload(outFile);
				sipArchive.setContent(contentManager.createMajor(currentUser, sipFileName, summary));
				sipArchive.setUser(currentUser);
				sipArchive.setCreatedBy(currentUser.getId());
				sipArchive.setCreationDate(new LocalDateTime());
				Transaction transaction = new Transaction();
				transaction.add(sipArchive);
				modelLayerFactory.newRecordServices().execute(transaction);

				if (deleteFiles) {
					for (String documentIds : ids) {
						try {
							Record record = recordServices.getDocumentById(documentIds);
							recordServices.logicallyDelete(record, currentUser);
							recordServices.physicallyDelete(record, currentUser,
									new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
						} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
							e.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
				ioServices.deleteQuietly(outFile);
				ioServices.deleteQuietly(outFolder);
			}
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

	public ProgressInfo getProgressInfo() {
		return progressInfo;
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