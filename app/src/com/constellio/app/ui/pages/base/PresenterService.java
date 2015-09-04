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
package com.constellio.app.ui.pages.base;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class PresenterService {
	private ModelLayerFactory modelLayerFactory;

	public PresenterService(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public User getCurrentUser(SessionContext sessionContext) {
		User currentUser;
		UserVO currentUserVO = sessionContext.getCurrentUser();
		String currentCollection = sessionContext.getCurrentCollection();
		if (currentUserVO != null) {
			currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserVO.getUsername(), currentCollection);
		} else {
			currentUser = null;
		}
		return currentUser;
	}

	@Deprecated
	public RecordVO getRecordVO(String id, VIEW_MODE viewMode) {
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return voBuilder.build(getRecord(id), viewMode);
	}

	public RecordVO getRecordVO(String id, VIEW_MODE viewMode, SessionContext sessionContext) {
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return voBuilder.build(getRecord(id), viewMode, sessionContext);
	}

	public Record getRecord(String id) {
		return modelLayerFactory.newRecordServices().getDocumentById(id);
	}
}

