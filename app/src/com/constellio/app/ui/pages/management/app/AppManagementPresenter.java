/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.management.app;

import java.io.OutputStream;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.io.streamFactories.StreamFactory;

@SuppressWarnings("serial")
public class AppManagementPresenter extends BasePresenter<AppManagementView> {

	public AppManagementPresenter(AppManagementView view) {
		super(view);
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
			appManagementService().update(new ProgressInfo());
		} catch (AppManagementServiceException e) {
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