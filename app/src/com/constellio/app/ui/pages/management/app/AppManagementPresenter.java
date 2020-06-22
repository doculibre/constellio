package com.constellio.app.ui.pages.management.app;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

@SuppressWarnings("serial")
public class AppManagementPresenter extends BasePresenter<AppManagementView> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppManagementPresenter.class);

	public AppManagementPresenter(AppManagementView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.hasAny(CorePermissions.MANAGE_SYSTEM_UPDATES).globally();
	}

	public boolean hasUpdatePermission() {
		if (TenantUtils.isSupportingTenants()) {
			return Toggle.ENABLE_CLOUD_SYSADMIN_FEATURES.isEnabled();
		}
		return true;
	}

	public void restartApplicationButtonClicked() {
		try {
			if (hasUpdatePermission()) {
				appManagementService().restart();
			} else {
				ConstellioFactories.clear();
				ConstellioFactories.getInstance();
			}
		} catch (AppManagementServiceException e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
			// FIXME No reference to Vaadin objects
			// view.getErrorHandler().error(new ErrorEvent(e));
		}
	}

	public void updateApplicationButtonClicked() {
		try {
			appManagementService().update(new ProgressInfo());
		} catch (AppManagementServiceException e) {
			LOGGER.warn("Error when updating", e);
			view.showErrorMessage(MessageUtils.toMessage(e));
			// FIXME No reference to Vaadin objects
			// view.getErrorHandler().error(new ErrorEvent(e));
		}
	}

	public void onSuccessfullUpload() {
		boolean canUpdate = appManagementService().isWarFileUploaded();
		view.setUpdateButtonVisible(canUpdate);
	}

	public void enterView() {
		boolean canUpdate = appManagementService().isWarFileUploaded();
		view.setUpdateButtonVisible(canUpdate);

		StreamFactory<OutputStream> warFileDestination = appManagementService().getWarFileDestination();
		view.setWarFileDestination(warFileDestination);

		//		view.setWarVersion(getApplicationServices().getWarVersion());
		view.setWebappName(appManagementService().getWebappFolderName());
	}

	private AppManagementService appManagementService() {
		return appLayerFactory.newApplicationService();
	}
}