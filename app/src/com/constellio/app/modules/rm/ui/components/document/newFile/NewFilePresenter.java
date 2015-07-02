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
package com.constellio.app.modules.rm.ui.components.document.newFile;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.ui.util.NewFileUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;

public class NewFilePresenter implements Serializable {
	
	private NewFileWindow window;
	
	public NewFilePresenter(NewFileWindow window) {
		this.window = window;
		
		List<String> supportedExtensions = NewFileUtils.getSupportedExtensions();
		window.setSupportedExtensions(supportedExtensions);
	}

	public void newFileNameSubmitted() {
		String fileName = window.getFileName();
		String extension = window.getExtension();
		if (StringUtils.isNotBlank(fileName) && StringUtils.isNotBlank(extension) && !fileName.endsWith("." + extension)) {
			fileName += "." + extension;
		}
		if (isFilenameValid(fileName)) {
			Content newFileContent = createNewFile(fileName);
			window.notifyNewFileCreated(newFileContent);
		} else {
			window.showErrorMessage("NewFileWindow.invalidFileName", fileName != null ? fileName : "");
		}
	}
	
	private boolean isFilenameValid(String fileName) {
		List<String> supportedExtensions = NewFileUtils.getSupportedExtensions();
		String extension = FilenameUtils.getExtension(fileName);
		return supportedExtensions.contains(extension);
	}
	
	private Content createNewFile(String fileName) {
		String extension = FilenameUtils.getExtension(fileName);
		ModelLayerFactory modelLayerFactory = window.getConstellioFactories().getModelLayerFactory();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		UserServices userServices = modelLayerFactory.newUserServices();
		
		String collection = window.getSessionContext().getCurrentCollection();
		String username = window.getSessionContext().getCurrentUser().getUsername();
		User user = userServices.getUserRecordInCollection(username, collection);
		
		InputStream newFileInput = NewFileUtils.newFile(extension);
		try {
			ContentVersionDataSummary dataSummary = contentManager.upload(newFileInput);
			return contentManager.createMinor(user, fileName, dataSummary);
		} finally {
			IOUtils.closeQuietly(newFileInput);
		}
	}

}
