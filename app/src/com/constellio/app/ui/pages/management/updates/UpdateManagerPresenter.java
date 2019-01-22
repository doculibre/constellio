package com.constellio.app.ui.pages.management.updates;

import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.CannotConnectToServer;
import com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause;
import com.constellio.app.services.recovery.UpgradeAppRecoveryService;
import com.constellio.app.services.recovery.UpgradeAppRecoveryServiceImpl;
import com.constellio.app.services.systemInformations.SystemInformationsService;
import com.constellio.app.servlet.ConstellioMonitoringServlet;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.constellio.app.services.migrations.VersionsComparator.isFirstVersionBeforeSecond;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.management.updates.UpdateNotRecommendedReason.BATCH_PROCESS_IN_PROGRESS;
import static java.util.Arrays.asList;

public class UpdateManagerPresenter extends BasePresenter<UpdateManagerView> {
	private SystemInformationsService systemInformationsService = new SystemInformationsService();

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateManagerPresenter.class);


	public UpdateManagerPresenter(UpdateManagerView view) {
		super(view);
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

	public boolean isLicensedForAutomaticUpdate() {
		return appLayerFactory.newApplicationService().isLicensedForAutomaticUpdate();
	}

	public LicenseInfo getLicenseInfo() {
		return appLayerFactory.newApplicationService().getLicenseInfo();
	}

	public boolean isAutomaticUpdateAvailable() {
		return isLicensedForAutomaticUpdate() && isFirstVersionBeforeSecond(getCurrentVersion(), getUpdateVersion());
	}

	public String getChangelog() {
		if (!appLayerFactory.getModelLayerFactory().getSystemConfigs().isUpdateServerConnectionEnabled()) {
			return null;
		}

		String changelog;
		try {
			changelog = appLayerFactory.newApplicationService().getChangelogFromServer();
		} catch (CannotConnectToServer cc) {
			changelog = null;
		}

		return changelog;
	}

	public String getUpdateVersion() {
		if (!appLayerFactory.getModelLayerFactory().getSystemConfigs().isUpdateServerConnectionEnabled()) {
			return "0";
		}

		try {
			return appLayerFactory.newApplicationService().getVersionFromServer();
		} catch (CannotConnectToServer cc) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.connection"));
			appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().setValue(
					ConstellioEIMConfigs.UPDATE_SERVER_CONNECTION_ENABLED, false);
			return "0";
		}
	}

	public void updateFromServer() {
		ProgressInfo progressInfo = view.openProgressPopup();
		try {
			appLayerFactory.newApplicationService().getWarFromServer(progressInfo);
			appLayerFactory.newApplicationService().update(progressInfo);
			view.showRestartRequiredPanel();

		} catch (CannotConnectToServer cc) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.connection"));
		} catch (AppManagementServiceException ase) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.file"));
		} finally {
			view.closeProgressPopup();

		}
	}

	public void restart() {
		try {
			appLayerFactory.newApplicationService().restart();
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			Record event = rm.newEvent()
					.setType(EventType.RESTARTING)
					.setUsername(getCurrentUser().getUsername())
					.setUserRoles(StringUtils.join(getCurrentUser().getAllRoles().toArray(), "; "))
					.setIp(getCurrentUser().getLastIPAddress())
					.setCreatedOn(TimeProvider.getLocalDateTime())
					.setTitle($("ListEventsView.restarting"))
					.getWrappedRecord();
			Transaction t = new Transaction();
			t.add(event);
			appLayerFactory.getModelLayerFactory().newRecordServices().execute(t);
		} catch (AppManagementServiceException | RecordServicesException ase) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.restart"));
		}
		ConstellioMonitoringServlet.systemRestarting = true;
		view.navigate().to().serviceMonitoring();
	}

	public void restartAndReindex() {
		FoldersLocator foldersLocator = new FoldersLocator();
		if (foldersLocator.getFoldersLocatorMode() == FoldersLocatorMode.PROJECT) {
			//Application is started from a test, it cannot be restarted
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			LOGGER.info("Reindexing started");
			ReindexingServices reindexingServices = modelLayerFactory.newReindexingServices();
			reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);
			LOGGER.info("Reindexing finished");
			Record eventRestarting = rm.newEvent()
					.setType(EventType.RESTARTING)
					.setUsername(getCurrentUser().getUsername())
					.setUserRoles(StringUtils.join(getCurrentUser().getAllRoles().toArray(), "; "))
					.setIp(getCurrentUser().getLastIPAddress())
					.setCreatedOn(TimeProvider.getLocalDateTime())
					.setTitle($("ListEventsView.restarting"))
					.getWrappedRecord();
			Record eventReindexing = rm.newEvent()
					.setType(EventType.REINDEXING)
					.setUsername(getCurrentUser().getUsername())
					.setUserRoles(StringUtils.join(getCurrentUser().getAllRoles().toArray(), "; "))
					.setIp(getCurrentUser().getLastIPAddress())
					.setCreatedOn(TimeProvider.getLocalDateTime())
					.setTitle($("ListEventsView.reindexing"))
					.getWrappedRecord();
			Transaction t = new Transaction();
			t.addAll(asList(eventReindexing, eventRestarting));
			try {

				appLayerFactory.getModelLayerFactory().newRecordServices().execute(t);
			} catch (Exception e) {
				view.showErrorMessage(e.getMessage());
			}
		} else {
			appLayerFactory.newApplicationService().markForReindexing();
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			Record eventRestarting = rm.newEvent()
					.setType(EventType.RESTARTING)
					.setUsername(getCurrentUser().getUsername())
					.setUserRoles(StringUtils.join(getCurrentUser().getAllRoles().toArray(), "; "))
					.setIp(getCurrentUser().getLastIPAddress())
					.setCreatedOn(TimeProvider.getLocalDateTime())
					.setTitle($("RedémarrageListEventsView.restarting"))
					.getWrappedRecord();
			Record eventReindexing = rm.newEvent()
					.setType(EventType.REINDEXING)
					.setUsername(getCurrentUser().getUsername())
					.setUserRoles(StringUtils.join(getCurrentUser().getAllRoles().toArray(), "; "))
					.setIp(getCurrentUser().getLastIPAddress())
					.setCreatedOn(TimeProvider.getLocalDateTime())
					.setTitle($("ListEventsView.reindexing"))
					.getWrappedRecord();
			Transaction t = new Transaction();
			t.addAll(asList(eventReindexing, eventRestarting));
			try {
				appLayerFactory.getModelLayerFactory().newRecordServices().execute(t);
			} catch (Exception e) {
				view.showErrorMessage(e.getMessage());
			}

			try {
				appLayerFactory.newApplicationService().restart();
			} catch (AppManagementServiceException ase) {
				view.showErrorMessage($("UpdateManagerViewImpl.error.restart"));
			}
		}
		ConstellioMonitoringServlet.systemRestarting = true;
		view.navigate().to().serviceMonitoring();
	}

	public void licenseUpdateRequested() {
		view.showLicenseUploadPanel();
	}

	public OutputStream getLicenseOutputStream() {
		FileOutputStream stream = null;
		try {
			File license = modelLayerFactory.getFoldersLocator().getUploadLicenseFile();
			stream = new FileOutputStream(license);
		} catch (FileNotFoundException fnfe) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.upload"));
		}
		return stream;
	}

	public void licenseUploadSucceeded() {
		appLayerFactory.newApplicationService().storeLicense(modelLayerFactory.getFoldersLocator().getUploadLicenseFile());
		view.showMessage($("UpdateManagerViewImpl.licenseUpdated"));
		view.navigate().to().updateManager();
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

	public boolean isRestartWithReindexButtonEnabled() {
		if (appLayerFactory.getModelLayerFactory().getDataLayerFactory().getDataLayerConfiguration().isSystemDistributed()) {
			return !UpgradeAppRecoveryServiceImpl.HAS_UPLOADED_A_WAR_SINCE_REBOOTING;
		} else {
			return !recoveryModeEnabled();
		}
	}

	private boolean recoveryModeEnabled() {
		return appLayerFactory.getModelLayerFactory().getSystemConfigs().isInUpdateProcess();
	}

	public boolean isUpdateEnabled() {
		UpdateRecoveryImpossibleCause updatePossible = isUpdateWithRecoveryPossible();
		return updatePossible == null || updatePossible == UpdateRecoveryImpossibleCause.TOO_SHORT_MEMORY;
	}

	public UpdateRecoveryImpossibleCause isUpdateWithRecoveryPossible() {
		return appLayerFactory.newUpgradeAppRecoveryService()
				.isUpdateWithRecoveryPossible();
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
}
