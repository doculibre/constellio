package com.constellio.app.ui.pages.management.updates;

import static com.constellio.app.services.migrations.VersionsComparator.isFirstVersionBeforeSecond;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.pages.management.updates.UpdateNotRecommendedReason.BATCH_PROCESS_IN_PROGRESS;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.CannotConnectToServer;
import com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause;
import com.constellio.app.services.recovery.UpgradeAppRecoveryService;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.utils.GradleFileVersionParser;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;

public class UpdateManagerPresenter extends BasePresenter<UpdateManagerView> {

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
		String changelog;
		try {
			changelog = appLayerFactory.newApplicationService().getChangelogFromServer();
		} catch (CannotConnectToServer cc) {
			changelog = null;
		}

		return changelog;
	}

	public String getUpdateVersion() {
		try {
			return appLayerFactory.newApplicationService().getVersionFromServer();
		} catch (CannotConnectToServer cc) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.connection"));
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
					.setTitle("new Restarting Event")
					.getWrappedRecord();
			Transaction t = new Transaction();
			t.add(event);
			appLayerFactory.getModelLayerFactory().newRecordServices().execute(t);
		} catch (AppManagementServiceException | RecordServicesException ase) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.restart"));
		}
	}

	public boolean isJava8Installed() {
		return System.getProperty("java.version").startsWith("1.8");
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
					.setTitle("new Restarting Event")
					.getWrappedRecord();
			Record eventReindexing = rm.newEvent()
					.setType(EventType.REINDEXING)
					.setUsername(getCurrentUser().getUsername())
					.setUserRoles(StringUtils.join(getCurrentUser().getAllRoles().toArray(), "; "))
					.setIp(getCurrentUser().getLastIPAddress())
					.setCreatedOn(TimeProvider.getLocalDateTime())
					.setTitle("new Reindexing Event")
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
			try {
				appLayerFactory.newApplicationService().restart();
			} catch (AppManagementServiceException ase) {
				view.showErrorMessage($("UpdateManagerViewImpl.error.restart"));
			}
		}
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
			version = GradleFileVersionParser.getVersion();
		}
		return version;
	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		return user.has(CorePermissions.MANAGE_SYSTEM_UPDATES).globally();
	}

	public boolean isRestartWithReindexButtonEnabled() {
		return !recoveryModeEnabled();
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
