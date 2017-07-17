package com.constellio.app.modules.tasks.ui.pages.workflowInstance;

import java.io.IOException;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.BetaWorkflowServices;
import com.constellio.app.modules.tasks.ui.builders.BetaWorkflowInstanceToVoBuilder;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowInstanceVO;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowTaskProgressionVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class BetaDisplayWorkflowInstancePresenter extends SingleSchemaBasePresenter<BetaDisplayWorkflowInstanceView> {
	private BetaWorkflowInstanceVO workflowInstanceVO;
	private List<BetaWorkflowTaskProgressionVO> workflowTaskProgressionVOs;
	private transient BetaWorkflowServices workflowServices;
	private transient TasksSchemasRecordsServices tasksSchemas;
	private BetaWorkflowInstanceToVoBuilder workflowInstanceToVoBuilder;

	public BetaDisplayWorkflowInstancePresenter(BetaDisplayWorkflowInstanceView view) {
		super(view, BetaWorkflow.DEFAULT_SCHEMA);
		initTransientObjects();
		workflowInstanceToVoBuilder = new BetaWorkflowInstanceToVoBuilder();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		workflowServices = new BetaWorkflowServices(collection, appLayerFactory);
		tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	void forParams(String params) {
		Record workflowInstanceRecord = getRecord(params);
		workflowInstanceVO = workflowInstanceToVoBuilder
				.build(workflowInstanceRecord, VIEW_MODE.DISPLAY, view.getSessionContext());
		view.setWorkflowInstanceVO(workflowInstanceVO);

		SessionContext sessionContext = view.getSessionContext();
		BetaWorkflowInstance workflowInstance = new BetaWorkflowInstance(workflowInstanceRecord, types());

		workflowTaskProgressionVOs = workflowServices.getRootModelTaskProgressionsVOs(workflowInstance, sessionContext);
		view.setWorkflowTaskProgressionVOs(workflowTaskProgressionVOs);
	}

	List<BetaWorkflowTaskProgressionVO> getChildren(BetaWorkflowTaskProgressionVO parent) {
		String workflowInstanceId = workflowInstanceVO.getId();
		Record workflowInstanceRecord = getRecord(workflowInstanceId);
		BetaWorkflowInstance workflowInstance = new BetaWorkflowInstance(workflowInstanceRecord, types());
		SessionContext sessionContext = view.getSessionContext();
		return workflowServices.getChildModelTaskProgressions(workflowInstance, parent.getWorkflowTaskVO(), sessionContext);
	}

	void backButtonClicked() {
		view.navigate().to(TaskViews.class).taskManagement();
	}

	boolean isFinished(BetaWorkflowTaskProgressionVO workflowTaskProgressionVO) {
		TaskVO taskVO = workflowTaskProgressionVO.getWorkflowTaskVO().getTaskVO();
		TaskStatus status = tasksSchemas.getTaskStatus(taskVO.getStatus());
		return status.isFinished();
	}

	boolean isTaskOverDue(BetaWorkflowTaskProgressionVO workflowTaskProgressionVO) {
		TaskVO task = workflowTaskProgressionVO.getWorkflowTaskVO().getTaskVO();
		LocalDate dueDate = task.getDueDate();
		if (dueDate == null) {
			return false;
		}
		if (dueDate.isBefore(TimeProvider.getLocalDate())) {
			LocalDate endDate = task.getEndDate();
			if (endDate == null || endDate.isAfter(dueDate)) {
				return true;
			}
		}
		return false;
	}
}
