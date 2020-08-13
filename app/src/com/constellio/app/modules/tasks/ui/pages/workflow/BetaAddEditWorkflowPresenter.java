package com.constellio.app.modules.tasks.ui.pages.workflow;

import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.ui.builders.BetaWorkflowToVoBuilder;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BetaAddEditWorkflowPresenter extends SingleSchemaBasePresenter<BetaAddEditWorkflowView> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BetaAddEditWorkflowPresenter.class);

	private boolean addView;

	private BetaWorkflowVO workflowVO;

	private BetaWorkflowToVoBuilder voBuilder;

	public BetaAddEditWorkflowPresenter(BetaAddEditWorkflowView view) {
		super(view, BetaWorkflow.DEFAULT_SCHEMA);
		initTransientObjects();
		voBuilder = new BetaWorkflowToVoBuilder();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(TasksPermissionsTo.MANAGE_WORKFLOWS).globally();
	}

	void forParams(String params) {
		addView = StringUtils.isBlank(params);
		view.setAddView(addView);

		Record workflowRecord;
		if (!addView) {
			String id = params;
			workflowRecord = getRecord(id);
		} else {
			workflowRecord = newRecord();
		}
		workflowVO = voBuilder.build(workflowRecord, VIEW_MODE.FORM, view.getSessionContext());
		view.setWorkflowVO(workflowVO);
	}

	void saveButtonClicked() {
		try {
			Record workflowRecord = toRecord(workflowVO);
			addOrUpdate(workflowRecord);
			view.navigate().to(TaskViews.class).displayWorkflow(workflowRecord.getId());
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
	}

	void cancelButtonClicked() {
		view.navigate().to(TaskViews.class).listWorkflows();
	}
}
