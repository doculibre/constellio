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
package com.constellio.app.modules.rm.ui.components.document.fields;

import java.io.Serializable;

import com.constellio.app.modules.rm.ui.components.document.newFile.NewFileWindow;
import com.constellio.app.ui.entities.ContentVersionVO;

public interface DocumentContentField extends CustomDocumentField<ContentVersionVO> {
	
	void setNewFileButtonVisible(boolean visible);
	
	void addNewFileClickListener(NewFileClickListener listener);
	
	void removeNewFileClickListener(NewFileClickListener listener);
	
	NewFileWindow getNewFileWindow();
	
	boolean isMajorVersionFieldVisible();

	void setMajorVersionFieldVisible(boolean visible);
	
	public static interface NewFileClickListener extends Serializable {
		
		void newFileClicked();
		
	}
	
}
