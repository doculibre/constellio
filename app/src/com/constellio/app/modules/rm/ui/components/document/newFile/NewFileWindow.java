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

import java.io.Serializable;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;

public interface NewFileWindow extends Serializable {
	
	String getFileName();

	String getExtension();
	
	void showErrorMessage(String key, Object...args);

	void open();
	
	void close();
	
	SessionContext getSessionContext();
	
	ConstellioFactories getConstellioFactories();
	
	void setSupportedExtensions(List<String> extensions);
	
	void addNewFileCreatedListener(NewFileCreatedListener listener);
	
	void removeNewFileCreatedListener(NewFileCreatedListener listener);
	
	void notifyNewFileCreated(Content content);
	
	public static interface NewFileCreatedListener extends Serializable {
		
		void newFileCreated(Content content);
		
	}

}
