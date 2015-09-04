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
package com.constellio.app.modules.rm.ui.pages.document;

import java.util.List;

import com.constellio.app.modules.rm.ui.components.document.DocumentActionsComponent;
import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface DisplayDocumentView extends BaseView, RecordsManagementViewGroup, DocumentActionsComponent {

	void refreshMetadataDisplay();

	void setContentVersions(List<ContentVersionVO> contentVersions);

	void setTasks(RecordVODataProvider tasksDataProvider);
}
