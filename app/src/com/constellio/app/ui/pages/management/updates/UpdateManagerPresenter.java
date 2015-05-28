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
package com.constellio.app.ui.pages.management.updates;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.CannotConnectToServer;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileNotFound;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

public class UpdateManagerPresenter extends BasePresenter<UpdateManagerView> {
	public UpdateManagerPresenter(UpdateManagerView view) {
		super(view);
	}

	public OutputStream getOutputStreamFor(String filename, String mimeType) {
		FileOutputStream warUpload = null;
		try {
			File warFile = appLayerFactory.getModelLayerFactory().getFoldersLocator().getUploadConstellioWarFile();
			warUpload = new FileOutputStream(warFile);
		} catch (FileNotFoundException fnfe) {
			view.showError($("UpdateManagerViewImpl.error.upload"));
		}

		return warUpload;
	}

	public String getChangelog() {
		String changelog;
		try {
			changelog = appLayerFactory.newApplicationService().getChangelogFromServer();
			changelog = changelog.split("<version>")[1];
		} catch (CannotConnectToServer cc) {
			changelog = null;
		}

		return changelog;
	}

	public String getChangelogVersion() {
		try {
			String changelog = appLayerFactory.newApplicationService().getChangelogFromServer();
			return changelog.split("<version>")[0];
		} catch (CannotConnectToServer cc) {
			view.showError($("UpdateManagerViewImpl.error.connection"));
			return "0";
		}
	}

	public void updateFromServer(ProgressInfo progressInfo) {
		try {
			appLayerFactory.newApplicationService().getWarFromServer(progressInfo);
			appLayerFactory.newApplicationService().update(progressInfo);
		} catch (CannotConnectToServer cc) {
			view.showError($("UpdateManagerViewImpl.error.connection"));
		} catch (AppManagementServiceException ase) {
			view.showError($("UpdateManagerViewImpl.error.file"));
		}
	}

	public void restart() {
		try {
			appLayerFactory.newApplicationService().restart();
		} catch (AppManagementServiceException ase) {
			view.showError($("UpdateManagerViewImpl.error.restart"));
		}
	}

	public String getCurrentVersion() {
		return appLayerFactory.newApplicationService().getWarVersion();
	}

	/*
	public String getBuildVersion() {
		String version;
		File data = appLayerFactory.getModelLayerFactory().getFoldersLocator().getBuildDataFile();
		FileService fileService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService();
		try {
			version = fileService.readFileToString(data);
		} catch (IOException ioe) {
			version = $("UpdateManagerViewImpl.error.buildDate");
		}
		return version;
	}
	*/

	public void uploadSucceeded(ProgressInfo progressInfo) {
		try {
			appLayerFactory.newApplicationService().update(progressInfo);
		} catch (AppManagementServiceException ase) {
			view.showError($("UpdateManagerViewImpl.error.file"));
		} catch (WarFileNotFound e) {
			view.showError($("UpdateManagerViewImpl.error.upload"));
		}
	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		return user.has(CorePermissions.MANAGE_SYSTEM_UPDATES).globally();

	}
}