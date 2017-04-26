package com.constellio.app.modules.rm.ui.pages.agent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.navigation.RMViews;
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
		return user.has(CorePermissions.VIEW_EVENTS).onSomething();
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

	public void backButtonClicked() {
		view.navigate().to(RMViews.class).eventAudit();
	}

}
