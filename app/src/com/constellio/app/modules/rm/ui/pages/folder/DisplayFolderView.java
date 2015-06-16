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
package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.ui.entities.ComponentState;
import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface DisplayFolderView extends BaseView, RecordsManagementViewGroup {

	void setRecord(RecordVO recordVO);

	void setDocuments(RecordVODataProvider dataProvider);

	void setSubFolders(RecordVODataProvider dataProvider);

	void selectMetadataTab();

	void selectDocumentsTab();

	void selectSubFoldersTab();

	void setLogicallyDeletable(ComponentState state);

	void setEditButtonState(ComponentState state);

	void setAddDocumentButtonState(ComponentState state);

	void setAddSubFolderButtonState(ComponentState state);

	void setDuplicateFolderButtonState(ComponentState state);

	void setPrintButtonState(ComponentState state);

	void setShareFolderButtonState(ComponentState state);

	void setAuthorizationButtonState(ComponentState authorizationButtonState);

	void setBorrowButtonState(ComponentState state);

	void setReturnFolderButtonState(ComponentState state);

	void setBorrowedMessage(String borrowedMessage);
}
