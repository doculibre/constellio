package com.constellio.app.ui.pages.management.updates;

import static com.constellio.app.services.migrations.VersionsComparator.isFirstVersionBeforeSecond;
import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.CannotConnectToServer;
import com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause;
import com.constellio.app.services.recovery.UpgradeAppRecoveryService;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.utils.GradleFileVersionParser;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

public class UpdateManagerPresenter extends BasePresenter<UpdateManagerView> {
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
		} catch (AppManagementServiceException ase) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.restart"));
		}
	}

	public void restartAndReindex() {
		appLayerFactory.newApplicationService().markForReindexing();
		try {
			appLayerFactory.newApplicationService().restart();
		} catch (AppManagementServiceException ase) {
			view.showErrorMessage($("UpdateManagerViewImpl.error.restart"));
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
		view.navigateTo().updateManager();
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
		return isUpdateWithRecoveryPossible() == null;
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
}
