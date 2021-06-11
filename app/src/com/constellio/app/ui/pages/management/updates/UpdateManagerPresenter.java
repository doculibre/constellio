package com.constellio.app.ui.pages.management.updates;

import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.CannotConnectToServer;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileNotFoundException;
import com.constellio.app.services.background.UpdateServerPingBackgroundAction;
import com.constellio.app.services.migrations.VersionsComparator;
import com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause;
import com.constellio.app.services.recovery.UpgradeAppRecoveryService;
import com.constellio.app.services.recovery.UpgradeAppRecoveryServiceImpl;
import com.constellio.app.services.systemInformations.SystemInformationsService;
import com.constellio.app.servlet.ConstellioMonitoringServlet;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.fields.download.DownloadThread;
import com.constellio.app.ui.framework.components.fields.download.InvalidWarUrlException;
import com.constellio.app.ui.framework.components.fields.download.TempFileDownload;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.FoldersLocatorMode;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexationParams;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.vaadin.ui.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.constellio.app.services.migrations.VersionsComparator.isFirstVersionBeforeSecond;
import static com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause.TOO_SHORT_SPACE;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.management.updates.UpdateNotRecommendedReason.BATCH_PROCESS_IN_PROGRESS;
import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;

@Slf4j
public class UpdateManagerPresenter extends BasePresenter<UpdateManagerView> {
	private SystemInformationsService systemInformationsService = new SystemInformationsService();

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateManagerPresenter.class);

	private static final String SYSTEM_LOG_FILE_NAME = "system.log";
	private static final String FILE_NAME_PARAMETER_KEY = "fileName";

	private DownloadThread downloadThread;
	private Set<File> tempFiles;

	public UpdateManagerPresenter(UpdateManagerView view) {
		super(view);
		tempFiles = new HashSet<>();
	}

	public boolean isAlternateUpdateAvailable() {
		return appSystemExtentions.alternateUpdateMode.isActive();
	}

	public String getAlternateUpdateName() {
		return appSystemExtentions.alternateUpdateMode.getCode();
	}

	public void standardUpdateRequested() {
		view.showStandardUpdatePanel();
	}

	public void alternateUpdateRequested() {
		UpdateModeHandler handler = appSystemExtentions.alternateUpdateMode.getHandler(view);
		view.showAlternateUpdatePanel(handler);
	}

	public void backButtonClicked() {
		view.navigate().to().previousView();
	}

	public boolean isLicensedForAutomaticUpdate() {
		return appLayerFactory.newApplicationService().isLicensedForAutomaticUpdate();
	}

	public LicenseInfo getLicenseInfo() {
		return appLayerFactory.newApplicationService().getLicenseInfo();
	}

	public boolean isAutomaticUpdateAvailable(String version) {
		return isLicensedForAutomaticUpdate() && isFirstVersionBeforeSecond(getCurrentVersion(), version);
	}

	public List<String> getAvailableLtsVersions() {
		List<String> versions = new ArrayList<>(UpdateServerPingBackgroundAction.newAvailableVersions);
		versions.sort(new VersionsComparator());
		Collections.reverse(versions);
		return versions;
	}

	public boolean hasAvailableLtsVersions() {
		return !getAvailableLtsVersions().isEmpty();
	}

	public String downloadReleaseNoteRequested(String version) {
		String releaseNote = null;
		try {
			releaseNote = appLayerFactory.newApplicationService().getReleaseNoteFromServer(version, getCurrentLocale());
			if (releaseNote == null) {
				view.showErrorMessage($("UpdateManagerViewImpl.error.connection"));
			}
		} catch (CannotConnectToServer cannotConnectToServer) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.connection"));
		}

		return releaseNote;
	}

	public void downloadWarAndInstalledClicked(String version) {
		String warDownloadLink = getWarDownloadLink(version);
		if (warDownloadLink != null) {
			ProgressBar downloadProgressBar = view.getDownloadProgressBar();
			TempFileDownload tempFileDownload = null;
			try {
				if (downloadThread == null) {
					URL downloadUrl = new URL(warDownloadLink);
					tempFileDownload = new TempFileDownload(downloadUrl);
					tempFiles.add(tempFileDownload.getTempFile());
					URLConnection successOnHeaders = tempFileDownload.getCon();

					downloadProgressBar.setVisible(true);
					downloadProgressBar.setIndeterminate(false);
					downloadProgressBar.setValue(0.0f);

					downloadThread = new DownloadThread(downloadProgressBar, successOnHeaders, tempFileDownload, ConstellioUI.getCurrent()) {
						@Override
						public void callback(TempFileDownload finishedTempFileDownload) {
							downloadFinished(finishedTempFileDownload);
						}
					};
					downloadThread.start();
				}
			} catch (InvalidWarUrlException | IOException ex) {
				log.debug("Download failed", ex);
				if (downloadThread != null) {
					downloadThread.interrupt();
					downloadThread = null;
				}
				if (tempFileDownload != null) {
					tempFileDownload.delete();
				}
			}
		} else {
			view.showErrorMessage($("UpdateManagerViewImpl.error.nullDownloadLink"));
		}
	}

	private void downloadFinished(TempFileDownload finishedTempFileDownload) {

		if (finishedTempFileDownload != null && finishedTempFileDownload.getTempFile().exists()) {
			File tempWar = finishedTempFileDownload.getTempFile();

			try {
				File warFile = appLayerFactory.getModelLayerFactory().getFoldersLocator()
						.getUploadConstellioWarFile();
				FileUtils.copyFile(tempWar, warFile);

				ProgressInfo progressInfo = view.openProgressPopup();
				try {
					appLayerFactory.newApplicationService().update(progressInfo);
					view.showRestartRequiredPanel();
				} catch (AppManagementServiceException ase) {
					view.showErrorMessage($("UpdateManagerViewImpl.error.file"));
				} catch (WarFileNotFoundException e) {
					view.showErrorMessage($("UpdateManagerViewImpl.error.upload"));
				} finally {
					view.closeProgressPopup();
					finishedTempFileDownload.delete();
				}
			} catch (IOException e) {
				view.showErrorMessage($("UpdateManagerViewImpl.error.file"));
				finishedTempFileDownload.delete();
			}
		} else {
			view.showErrorMessage($("UpdateManagerViewImpl.error.connection"));
		}
	}

	public void onPageExit() {
		if (downloadThread != null) {
			downloadThread.interrupt();
			downloadThread = null;
		}
		tempFiles.forEach(FileUtils::deleteQuietly);
	}

	private String getWarDownloadLink(String version) {
		String warDownloadLink = null;
		try {
			warDownloadLink = appLayerFactory.newApplicationService().getWarDownloadLinkFromServer(version);
			if (warDownloadLink == null) {
				view.showErrorMessage($("UpdateManagerViewImpl.error.connection"));
			}
		} catch (CannotConnectToServer cannotConnectToServer) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.connection"));
		}

		return warDownloadLink;
	}

	public File getLastAlert() {
		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
		File lastAlert = manager.getFileFromValue(ConstellioEIMConfigs.LOGIN_NOTIFICATION_ALERT, "lastAlert.pdf");

		return lastAlert;
	}

	public Object getLastAlertConfigValue() {
		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
		return manager.getValue(ConstellioEIMConfigs.LOGIN_NOTIFICATION_ALERT);
	}

	public boolean hasLastAlertPermission() {
		return getCurrentUser().has(CorePermissions.VIEW_LOGIN_NOTIFICATION_ALERT).globally();
	}

	public String getLicenseInfoPageUrl() {
		String pagesUrl = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.LICENSE_INFO_PAGES_URL);
		if (StringUtils.isNotBlank(pagesUrl)) {
			String[] pages = pagesUrl.split(";");
			if (pages.length == 2) {
				String pageUrl = pages[getCurrentLocale() == Locale.FRENCH ? 0 : 1];
				String fragment = "";

				int firstBracket = pagesUrl.indexOf("[");
				int lastBracket = pageUrl.lastIndexOf("]");
				if (firstBracket != -1 && lastBracket != -1) {
					String fragmentsPossibility = pageUrl.substring(firstBracket, lastBracket);
					pageUrl = pagesUrl.substring(0, firstBracket - 1);

					String[] fragments = fragmentsPossibility.split("|");
					if (fragments.length == 3) {
						switch (getLicenseInfo().getSupportPlan()) {
							case SILVER:
								fragment = fragments[0];
								break;
							case GOLD:
								fragment = fragments[1];
								break;
							case PLATINUM:
								fragment = fragments[2];
								break;
						}
					}
				}

				return pageUrl + fragment;
			}
		}

		return null;
	}

	//	public void updateFromServer() {
	//		ProgressInfo progressInfo = view.openProgressPopup();
	//		try {
	//			appLayerFactory.newApplicationService().getWarFromServer(progressInfo);
	//			appLayerFactory.newApplicationService().update(progressInfo);
	//			view.showRestartRequiredPanel();
	//
	//		} catch (CannotConnectToServer cc) {
	//			view.showErrorMessage($("UpdateManagerViewImpl.error.connection"));
	//		} catch (AppManagementServiceException ase) {
	//			view.showErrorMessage($("UpdateManagerViewImpl.error.file"));
	//		} finally {
	//			view.closeProgressPopup();
	//
	//		}
	//	}

	public void restartAndRebuildCache() {
		appLayerFactory.newApplicationService().markCacheForRebuild();
		restart();
	}

	public void restartAndReindex(boolean repopulate) {
		appLayerFactory.getSystemGlobalConfigsManager().blockSystemDuringReindexing();
		if (isFromTest()) {
			restartFromTest(repopulate);
		} else {
			appLayerFactory.newApplicationService().markForReindexing();
			appLayerFactory.newApplicationService().markCacheForRebuildIfRequired();

			logReindexingEvent();
			restart();
		}
	}

	public void restart() {
		logRestartingEvent();

		try {
			if (hasUpdatePermission()) {
				appLayerFactory.newApplicationService().restart();
			} else {
				appLayerFactory.newApplicationService().restartTenant();
			}
		} catch (AppManagementServiceException e) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.restart"));
		}

		view.navigate().to().serviceMonitoring();
	}

	private boolean isFromTest() {
		if (FoldersLocator.usingAppWrapper()) {
			File systemLogFile = getSystemLogFile();
			if (systemLogFile != null && systemLogFile.exists()) {
				if (!FileUtils.deleteQuietly(systemLogFile)) {
					view.showErrorMessage($("UpdateManagerViewImpl.error.fileNotDeleted"));
				}
			}
		}

		FoldersLocator foldersLocator = new FoldersLocator();
		return foldersLocator.getFoldersLocatorMode() == FoldersLocatorMode.PROJECT;
	}

	private void restartFromTest(boolean repopulate) {
		//Application is started from a test, it cannot be restarted
		LOGGER.info("Reindexing started");
		ReindexingServices reindexingServices = modelLayerFactory.newReindexingServices();
		reindexingServices.reindexCollections(new ReindexationParams(RECALCULATE_AND_REWRITE)
				.setRepopulate(repopulate));
		LOGGER.info("Reindexing finished");

		logRestartingEvent();
		logReindexingEvent();

		ConstellioMonitoringServlet.systemRestarting = true;
		view.navigate().to().serviceMonitoring();
	}

	private void logRestartingEvent() {
		logRestartingOrReindexingEvent(EventType.RESTARTING, "ListEventsView.restarting");
	}

	private void logReindexingEvent() {
		logRestartingOrReindexingEvent(EventType.REINDEXING, "ListEventsView.reindexing");
	}

	private void logRestartingOrReindexingEvent(String eventType, String locId) {
		try {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			Record event = rm.newEvent()
					.setType(eventType)
					.setUsername(getCurrentUser().getUsername())
					.setUserRoles(StringUtils.join(getCurrentUser().getAllRoles().toArray(), "; "))
					.setIp(getCurrentUser().getLastIPAddress())
					.setCreatedOn(TimeProvider.getLocalDateTime())
					.setTitle($(locId))
					.getWrappedRecord();
			Transaction t = new Transaction();
			t.add(event);
			appLayerFactory.getModelLayerFactory().newRecordServices().execute(t);
		} catch (RecordServicesException e) {
			view.showErrorMessage(e.getMessage());
		}
	}

	private File getSystemLogFile() {
		File systemLogFile = null;

		File logsFolder = getFoldersLocator().getLogsFolder();
		if (logsFolder.exists()) {
			File tempFile = new File(logsFolder, SYSTEM_LOG_FILE_NAME);
			if (tempFile.exists()) {
				systemLogFile = tempFile;
			} else {
				HashMap<String, Object> i18nParameters = buildSingleValueParameters(FILE_NAME_PARAMETER_KEY, SYSTEM_LOG_FILE_NAME);
				view.showErrorMessage($("UpdateManagerViewImpl.error.fileNotFound", i18nParameters));
			}
		}

		return systemLogFile;
	}

	private HashMap<String, Object> buildSingleValueParameters(String key, Object value) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put(key, value);
		return parameters;
	}


	public void licenseUpdateRequested() {
		view.showLicenseUploadPanel();
	}

	public OutputStream getLicenseOutputStream() {
		FileOutputStream stream = null;
		try {
			File license = getFoldersLocator().getUploadLicenseFile();
			stream = new FileOutputStream(license);
		} catch (FileNotFoundException fnfe) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.upload"));
		}
		return stream;
	}

	public void licenseUploadSucceeded() {
		File uploadedLicense = getFoldersLocator().getUploadLicenseFile();
		if (isValidLicense(uploadedLicense)) {
			storeLicense(uploadedLicense);
			view.showMessage($("UpdateManagerViewImpl.licenseUpdated"));
			refreshView();
		} else {
			view.showErrorMessage($("UpdateManagerViewImpl.invalidLicense"));
		}
	}

	protected void refreshView() {
		view.navigate().to().updateManager();
	}

	protected void storeLicense(File uploadedLicense) {
		appLayerFactory.newApplicationService().storeLicense(uploadedLicense);
	}

	protected FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}

	private boolean isValidLicense(File license) {
		try {
			Document parseDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(license);
			org.jdom2.Document document = new DOMBuilder().build(parseDocument);
			String expectedSignature = document.getRootElement().getChildText("signature");
			document.getRootElement().getChild("signature").detach();

			EncryptionServices encryptionServices = appLayerFactory.getModelLayerFactory().newEncryptionServices();
			File publicVerificationKey = getFoldersLocator().getVerificationKey();
			PublicKey publicKey = encryptionServices.createPublicKeyFromFile(publicVerificationKey);

			return encryptionServices.verify(new XMLOutputter(Format.getCompactFormat()).outputString(document), expectedSignature, publicKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void licenseUploadCancelled() {
		view.showStandardUpdatePanel();
	}

	public String getCurrentVersion() {
		String version = appLayerFactory.newApplicationService().getWarVersion();
		if (version == null || version.equals("5.0.0")) {
			File versionFile = new File(new FoldersLocator().getConstellioProject(), "version");
			if (versionFile.exists()) {
				try {
					version = FileUtils.readFileToString(versionFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				version = "no version file";
			}
		}
		return version;
	}

	public String getLinuxVersion() {
		return systemInformationsService.getLinuxVersion();
	}

	public boolean isLinuxVersionDeprecated(String version) {
		return StringUtils.isBlank(version) || systemInformationsService.isLinuxVersionDeprecated(version);
	}

	public boolean isPrivateRepositoryInstalled() {
		return systemInformationsService.isPrivateRepositoryInstalled();
	}

	public boolean isSolrUserRoot(String solrUser) {
		return StringUtils.isBlank(solrUser) || solrUser.equals("root");
	}

	public boolean isConstellioUserRoot(String constellioUser) {
		return StringUtils.isBlank(constellioUser) || constellioUser.equals("root");
	}

	public String getConstellioUser() {
		return systemInformationsService.getConstellioUser();
	}

	public String getSolrUser() {
		return systemInformationsService.getSolrUser();
	}

	public String getJavaVersion() {
		return systemInformationsService.getJavaVersion();
	}

	public String getWrapperJavaVersion() {
		return systemInformationsService.getWrapperJavaVersion();
	}

	public boolean isJavaVersionDeprecated(String version) {
		return StringUtils.isBlank(version) || systemInformationsService.isJavaVersionDeprecated(version);
	}

	public String getSolrVersion() {
		return systemInformationsService.getSolrVersion();
	}

	public boolean isSolrVersionDeprecated(String version) {
		return StringUtils.isBlank(version) || systemInformationsService.isSolrVersionDeprecated(version);
	}

	public String getDiskUsage(String path) {
		return systemInformationsService.getDiskUsage(path);
	}

	public boolean isDiskUsageProblematic(String diskUsage) {
		return StringUtils.isBlank(diskUsage) || systemInformationsService.isDiskUsageProblematic(diskUsage);
	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		return user.has(CorePermissions.MANAGE_SYSTEM_UPDATES).globally();
	}

	public boolean hasUpdatePermission() {
		if (TenantUtils.isSupportingTenants()) {
			return Toggle.ENABLE_CLOUD_SYSADMIN_FEATURES.isEnabled();
		}
		return true;
	}

	public boolean isRestartWithReindexButtonEnabled() {
		if (appLayerFactory.isReindexing()) {
			return false;
		}

		if (appLayerFactory.getModelLayerFactory().getDataLayerFactory().getDataLayerConfiguration().isSystemDistributed()) {
			return !UpgradeAppRecoveryServiceImpl.HAS_UPLOADED_A_WAR_SINCE_REBOOTING || !FoldersLocator.usingAppWrapper();
		} else {
			return !recoveryModeEnabled() || !FoldersLocator.usingAppWrapper();
		}
	}

	public boolean isRestartWithCacheRebuildEnabled() {
		return !appLayerFactory.isReindexing();
	}

	private boolean recoveryModeEnabled() {
		return !appLayerFactory.getModelLayerFactory().getDataLayerFactory().isDistributed()
			   && !TenantUtils.isSupportingTenants()
			   && appLayerFactory.getModelLayerFactory().getSystemConfigs().isInUpdateProcess();
	}

	public boolean isUpdateEnabled() {
		//Warning are prefered to feature disability
		return true;
	}

	public UpdateRecoveryImpossibleCause isUpdateWithRecoveryPossible() {
		if (isDiskUsageProblematic(getDiskUsage("/opt")) ||
			(isSolrDiskUsageValidationEnabled() && isDiskUsageProblematic(getDiskUsage("/var/solr")))) {
			return TOO_SHORT_SPACE;
		}
		return appLayerFactory.newUpgradeAppRecoveryService().isUpdateWithRecoveryPossible();
	}

	private boolean isSolrDiskUsageValidationEnabled() {
		return appLayerFactory.getModelLayerFactory().getSystemConfigs().isSystemStateSolrDiskUsageValidationEnabled();
	}

	public String getExceptionDuringLastUpdate() {
		UpgradeAppRecoveryService upgradeService = appLayerFactory
				.newUpgradeAppRecoveryService();
		return upgradeService.getLastUpgradeExceptionMessage();
	}

	public UpdateNotRecommendedReason getUpdateNotRecommendedReason() {
		if (modelLayerFactory.getBatchProcessesManager().getCurrentBatchProcess() != null && !modelLayerFactory
				.getBatchProcessesManager().getPendingBatchProcesses().isEmpty()) {
			return BATCH_PROCESS_IN_PROGRESS;
		} else {
			return null;
		}
	}

	public boolean isCurrentlyReindexingOnThisServer() {
		return ReindexingServices.getReindexingInfos() != null;
	}

	public void cancelReindexing() {
		appLayerFactory.getSystemGlobalConfigsManager().unblockSystemDuringReindexing();
		restart();
	}

	public boolean isDistributedEnvironment() {
		return !appLayerFactory.getModelLayerFactory().getDataLayerFactory().isDistributed();
	}
}
