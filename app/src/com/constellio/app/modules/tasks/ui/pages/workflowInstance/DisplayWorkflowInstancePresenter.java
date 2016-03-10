package com.constellio.app.modules.tasks.ui.pages.workflowInstance;

import java.io.IOException;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.tasks.model.wrappers.Workflow;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.WorkflowServices;
import com.constellio.app.modules.tasks.ui.builders.WorkflowInstanceToVoBuilder;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.modules.tasks.ui.entities.WorkflowInstanceVO;
import com.constellio.app.modules.tasks.ui.entities.WorkflowTaskProgressionVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class DisplayWorkflowInstancePresenter extends SingleSchemaBasePresenter<DisplayWorkflowInstanceView> {
	private WorkflowInstanceVO workflowInstanceVO;
	private List<WorkflowTaskProgressionVO> workflowTaskProgressionVOs;
	private transient WorkflowServices workflowServices;
	private transient TasksSchemasRecordsServices tasksSchemas;
	private WorkflowInstanceToVoBuilder workflowInstanceToVoBuilder;

	public DisplayWorkflowInstancePresenter(DisplayWorkflowInstanceView view) {
		super(view, Workflow.DEFAULT_SCHEMA);
		initTransientObjects();
		workflowInstanceToVoBuilder = new WorkflowInstanceToVoBuilder();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		workflowServices = new WorkflowServices(collection, appLayerFactory);
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
		WorkflowInstance workflowInstance = new WorkflowInstance(workflowInstanceRecord, types());

		workflowTaskProgressionVOs = workflowServices.getRootModelTaskProgressionsVOs(workflowInstance, sessionContext);
		view.setWorkflowTaskProgressionVOs(workflowTaskProgressionVOs);
	}

	List<WorkflowTaskProgressionVO> getChildren(WorkflowTaskProgressionVO parent) {
		String workflowInstanceId = workflowInstanceVO.getId();
		Record workflowInstanceRecord = getRecord(workflowInstanceId);
		WorkflowInstance workflowInstance = new WorkflowInstance(workflowInstanceRecord, types());
		SessionContext sessionContext = view.getSessionContext();
		return workflowServices.getChildModelTaskProgressions(workflowInstance, parent.getWorkflowTaskVO(), sessionContext);
	}

	void backButtonClicked() {
		view.navigate().to(TaskViews.class).taskManagement();
	}

	boolean isFinished(WorkflowTaskProgressionVO workflowTaskProgressionVO) {
		TaskVO taskVO = workflowTaskProgressionVO.getWorkflowTaskVO().getTaskVO();
		TaskStatus status = tasksSchemas.getTaskStatus(taskVO.getStatus());
		return status.isFinished();
	}

	boolean isTaskOverDue(WorkflowTaskProgressionVO workflowTaskProgressionVO) {
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
