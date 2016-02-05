package com.constellio.app.ui.pages.management.app;

import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

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

	public void restartApplicationButtonClicked() {
		try {
			appManagementService().restart();
		} catch (AppManagementServiceException e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
			// FIXME No reference to Vaadin objects
			// view.getErrorHandler().error(new ErrorEvent(e));
		}
	}

	public void updateApplicationButtonClicked() {
		try {
			view.showMessage("Je suis la bonne version");
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