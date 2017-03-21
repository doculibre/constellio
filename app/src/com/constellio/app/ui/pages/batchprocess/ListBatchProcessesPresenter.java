package com.constellio.app.ui.pages.batchprocess;

import java.util.List;

import com.constellio.app.ui.entities.BatchProcessVO;
import com.constellio.app.ui.framework.builders.BatchProcessToVOBuilder;
import com.constellio.app.ui.framework.data.BatchProcessDataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.batch.manager.BatchProcessesManager;

public class ListBatchProcessesPresenter extends BasePresenter<ListBatchProcessesView> {

	public ListBatchProcessesPresenter(ListBatchProcessesView view) {
		super(view);
		init();
	}
	
	private void init() {
		BatchProcessToVOBuilder voBuilder = new BatchProcessToVOBuilder();
		BatchProcessDataProvider userBatchProcessDataProvider = new BatchProcessDataProvider();
		BatchProcessDataProvider systemBatchProcessDataProvider = new BatchProcessDataProvider();
		
		User currentUser = getCurrentUser();
		String currentUsername = currentUser.getUsername();
		
		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		List<BatchProcess> nonFinishedBatchProcesses = batchProcessesManager.getAllNonFinishedBatchProcesses();
		for (int i = 0; i < nonFinishedBatchProcesses.size(); i++) {
			BatchProcess batchProcess = nonFinishedBatchProcesses.get(i);
			BatchProcessVO batchProcessVO = voBuilder.build(batchProcess);
			String batchProcessUsername = batchProcessVO.getUsername();
			if (batchProcessUsername.equals(currentUsername)) {
				userBatchProcessDataProvider.addBatchProcess(batchProcessVO);
			}
			systemBatchProcessDataProvider.addBatchProcess(batchProcessVO);
		}
		
		view.setUserBatchProcesses(userBatchProcessDataProvider);
		if (areSystemBatchProcessesVisible(currentUser)) {
			view.setSystemBatchProcesses(systemBatchProcessDataProvider);
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}
	
	private boolean areSystemBatchProcessesVisible(User user) {
		return user.has(CorePermissions.VIEW_SYSTEM_BATCH_PROCESSES).globally();
	}

}
