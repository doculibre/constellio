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
package com.constellio.app.modules.rm.ui.pages.agent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.ui.entities.AgentLogVO;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserPhotosServices;

public class ListAgentLogsPresenter extends BasePresenter<ListAgentLogsView> {

	public ListAgentLogsPresenter(ListAgentLogsView view) {
		super(view);
	}

	public ListAgentLogsPresenter(ListAgentLogsView view,
			ConstellioFactories constellioFactories,
			SessionContext sessionContext) {
		super(view, constellioFactories, sessionContext);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.VIEW_EVENTS).globally();
	}
	
	void showLogsButtonClicked() {
		String userId = view.getSelectedUserId();
		List<AgentLogVO> agentLogVOs = new ArrayList<>();

		if (StringUtils.isNotBlank(userId)) {
			ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getModelLayerFactory();
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			UserPhotosServices userPhotosServices = modelLayerFactory.newUserPhotosServices();
			
			Record userRecord = recordServices.getDocumentById(userId);
			User user = wrapUser(userRecord);
			String username = user.getUsername();
			
			List<String> filenames = new ArrayList<>(userPhotosServices.getUserLogs(username));
			// Sort by date ascending
			Collections.sort(filenames);
			// Sort by date descending
			Collections.reverse(filenames);
			for (String filename : filenames) {
				agentLogVOs.add(new AgentLogVO(username, filename));
			}
		}
		view.setAgentLogs(agentLogVOs);
	}

	public InputStream getInputStream(AgentLogVO agentLogVO) {
		String username = agentLogVO.getUsername();
		String filename = agentLogVO.getFilename();
		ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getModelLayerFactory();
		UserPhotosServices userPhotosServices = modelLayerFactory.newUserPhotosServices();
		return userPhotosServices.newUserLogInputStream(username, filename, "ListAgentLogsPresenter.getInputStream");
	}

}
